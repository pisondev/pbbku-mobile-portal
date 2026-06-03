package api

import (
	"net/http"
	"os"
	"strconv"
	"strings"

	"pbbku-mobile-portal/apps/api/internal/db"
)

type Options struct {
	AllowedOrigins  []string
	AdminAPIKey     string
	AllowPublicSave bool
	MaxBodyBytes    int64
}

func NewRouter(database *db.Database) http.Handler {
	return NewRouterWithOptions(database, OptionsFromEnv())
}

func OptionsFromEnv() Options {
	return Options{
		AllowedOrigins:  splitCSV(os.Getenv("PBBKU_ALLOWED_ORIGINS")),
		AdminAPIKey:     os.Getenv("PBBKU_ADMIN_API_KEY"),
		AllowPublicSave: strings.EqualFold(os.Getenv("PBBKU_ALLOW_PUBLIC_SAVE"), "true"),
		MaxBodyBytes:    int64FromEnv("PBBKU_MAX_BODY_BYTES", 1_048_576),
	}
}

func NewRouterWithOptions(database *db.Database, options Options) http.Handler {
	if options.MaxBodyBytes <= 0 {
		options.MaxBodyBytes = 1_048_576
	}
	server := &Server{db: database, options: options}
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", server.health)
	mux.HandleFunc("POST /api/rpc/objekPajak/search", server.searchObjekPajak)
	mux.HandleFunc("POST /api/rpc/objekPajak/listDetails", server.listObjekPajakDetails)
	mux.HandleFunc("POST /api/rpc/objekPajak/getByNop", server.getObjekPajakByNop)
	mux.HandleFunc("POST /api/rpc/objekPajak/save", server.requireAdmin(server.saveObjekPajak))
	mux.HandleFunc("POST /api/rpc/objekPajak/getSpptHistory", server.getSpptHistory)
	mux.HandleFunc("POST /api/rpc/objekPajak/getTunggakan", server.getTunggakan)
	mux.HandleFunc("POST /api/rpc/objekPajak/getNextNoUrut", server.getNextNoUrut)
	mux.HandleFunc("POST /api/rpc/objekPajak/getNextNoFormulir", server.getNextNoFormulir)
	mux.HandleFunc("POST /api/rpc/lspop/listByNop", server.listBuildingsByNop)
	mux.HandleFunc("POST /api/rpc/lspop/getBuilding", server.getBuilding)
	mux.HandleFunc("POST /api/rpc/lspop/listFasilitas", server.listFacilities)
	mux.HandleFunc("POST /api/rpc/lspop/nextNoBng", server.nextNoBng)
	mux.HandleFunc("POST /api/rpc/wilayah/listPropinsi", server.listPropinsi)
	mux.HandleFunc("POST /api/rpc/wilayah/listDati2", server.listDati2)
	mux.HandleFunc("POST /api/rpc/wilayah/listKecamatan", server.listKecamatan)
	mux.HandleFunc("POST /api/rpc/wilayah/listKelurahan", server.listKelurahan)
	mux.HandleFunc("POST /api/rpc/wilayah/listBlok", server.listBlok)
	mux.HandleFunc("POST /api/rpc/sppt/listByNop", server.listSpptByNop)
	mux.HandleFunc("POST /api/rpc/sppt/get", server.getSppt)
	mux.HandleFunc("POST /api/rpc/sppt/list", server.listSppt)

	mux.HandleFunc("GET /api/v1/wilayah/propinsi", server.restListPropinsi)
	mux.HandleFunc("GET /api/v1/wilayah/dati2", server.restListDati2)
	mux.HandleFunc("GET /api/v1/wilayah/kecamatan", server.restListKecamatan)
	mux.HandleFunc("GET /api/v1/wilayah/kelurahan", server.restListKelurahan)
	mux.HandleFunc("GET /api/v1/wilayah/blok", server.restListBlok)
	mux.HandleFunc("GET /api/v1/objek-pajak/search", server.restSearchObjekPajak)
	mux.HandleFunc("GET /api/v1/objek-pajak", server.restListObjekPajakDetails)
	mux.HandleFunc("GET /api/v1/objek-pajak/{nop}", server.restGetObjekPajakByNop)
	mux.HandleFunc("GET /api/v1/objek-pajak/{nop}/sppt", server.restListSpptByNop)
	mux.HandleFunc("GET /api/v1/objek-pajak/{nop}/tunggakan", server.restGetTunggakan)
	mux.HandleFunc("GET /api/v1/objek-pajak/{nop}/bangunan", server.restListBuildingsByNop)
	mux.HandleFunc("GET /api/v1/objek-pajak/{nop}/bangunan/{noBng}", server.restGetBuilding)
	mux.HandleFunc("GET /api/v1/objek-pajak/{nop}/bangunan/{noBng}/fasilitas", server.restListFacilities)
	mux.HandleFunc("GET /api/v1/sppt", server.restListSppt)
	mux.HandleFunc("GET /api/v1/sppt/{nop}/{year}", server.restGetSppt)
	return server.withMiddleware(mux)
}

func (s *Server) withMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		s.writeSecurityHeaders(w)
		if origin := s.allowedOrigin(r.Header.Get("Origin")); origin != "" {
			w.Header().Set("Access-Control-Allow-Origin", origin)
			w.Header().Set("Vary", "Origin")
		}
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
		w.Header().Set("Access-Control-Allow-Methods", "GET,POST,OPTIONS")
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}
		if r.Method == http.MethodPost {
			if contentType := r.Header.Get("Content-Type"); !strings.Contains(strings.ToLower(contentType), "application/json") {
				writeError(w, http.StatusUnsupportedMediaType, "Content-Type must be application/json")
				return
			}
			r.Body = http.MaxBytesReader(w, r.Body, s.options.MaxBodyBytes)
		}
		next.ServeHTTP(w, r)
	})
}

func splitCSV(value string) []string {
	parts := strings.Split(value, ",")
	result := make([]string, 0, len(parts))
	for _, part := range parts {
		trimmed := strings.TrimSpace(part)
		if trimmed != "" {
			result = append(result, trimmed)
		}
	}
	return result
}

func int64FromEnv(key string, fallback int64) int64 {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}
	parsed, err := strconv.ParseInt(value, 10, 64)
	if err != nil {
		return fallback
	}
	return parsed
}
