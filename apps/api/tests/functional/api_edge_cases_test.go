package functional_test

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"sync"
	"testing"

	"pbbku-mobile-portal/apps/api/internal/api"
)

func TestSecurityAndBadRequestEdgeCases(t *testing.T) {
	router := newFunctionalRouter(t)

	health := assertStatus(t, router, http.MethodGet, "/health", nil, http.StatusOK)
	if health.Header().Get("X-Content-Type-Options") != "nosniff" {
		t.Fatalf("missing security header")
	}

	noContentType := httptestRequest(router, http.MethodPost, "/api/rpc/wilayah/listPropinsi", []byte(`{"json":{}}`), "", nil)
	if noContentType.Code != http.StatusUnsupportedMediaType {
		t.Fatalf("missing content-type status = %d, want 415; body=%s", noContentType.Code, noContentType.Body.String())
	}

	malformed := httptestRequest(router, http.MethodPost, "/api/rpc/wilayah/listDati2", []byte(`{"json":`), "application/json", nil)
	if malformed.Code != http.StatusBadRequest {
		t.Fatalf("malformed JSON status = %d, want 400; body=%s", malformed.Code, malformed.Body.String())
	}

	invalidNop := postJSONStatus(t, router, "/api/rpc/objekPajak/getByNop", map[string]any{
		"kdPropinsi": "5", "kdDati2": "71", "kdKecamatan": "010", "kdKelurahan": "001",
		"kdBlok": "054", "noUrut": "0032", "kdJnsOp": "0",
	}, http.StatusBadRequest)
	if invalidNop["error"] == nil {
		t.Fatalf("invalid NOP should return error body")
	}

	savePayload := validSavePayload("0033", "WP TANPA KEY")
	saveDenied := postJSONStatus(t, router, "/api/rpc/objekPajak/save", savePayload, http.StatusForbidden)
	if saveDenied["error"] == nil {
		t.Fatalf("save without admin should return error body")
	}

	assertStatus(t, router, http.MethodGet, "/api/rpc/wilayah/listPropinsi", nil, http.StatusMethodNotAllowed)
}

func TestCorsAndBodyLimitEdgeCases(t *testing.T) {
	router, _ := newFunctionalRouterWithOptions(t, api.Options{
		AllowedOrigins: []string{"https://pbbku-api.tierratie.com"},
		MaxBodyBytes:   32,
	})

	preflight := httptestRequest(
		router,
		http.MethodOptions,
		"/api/rpc/wilayah/listPropinsi",
		nil,
		"",
		map[string]string{"Origin": "https://pbbku-api.tierratie.com"},
	)
	if preflight.Code != http.StatusNoContent {
		t.Fatalf("preflight status = %d, want 204", preflight.Code)
	}
	if origin := preflight.Header().Get("Access-Control-Allow-Origin"); origin != "https://pbbku-api.tierratie.com" {
		t.Fatalf("allow-origin = %q, want configured origin", origin)
	}

	rejectedOrigin := httptestRequest(
		router,
		http.MethodOptions,
		"/api/rpc/wilayah/listPropinsi",
		nil,
		"",
		map[string]string{"Origin": "https://evil.example"},
	)
	if origin := rejectedOrigin.Header().Get("Access-Control-Allow-Origin"); origin != "" {
		t.Fatalf("unexpected allow-origin for rejected origin: %q", origin)
	}

	oversizedBody := []byte(`{"json":{"query":"payload larger than configured request body limit"}}`)
	oversized := httptestRequest(router, http.MethodPost, "/api/rpc/objekPajak/search", oversizedBody, "application/json", nil)
	if oversized.Code != http.StatusBadRequest {
		t.Fatalf("oversized body status = %d, want 400; body=%s", oversized.Code, oversized.Body.String())
	}
}

func TestAdminSaveRequiresKeyAndRunsInTransaction(t *testing.T) {
	router, _ := newFunctionalRouterWithOptions(t, api.Options{AdminAPIKey: "secret", MaxBodyBytes: 1_048_576})

	payload := validSavePayload("0033", "WP ADMIN")
	withoutKey := postJSONStatus(t, router, "/api/rpc/objekPajak/save", payload, http.StatusUnauthorized)
	if withoutKey["error"] == nil {
		t.Fatalf("save without key should return error")
	}

	withKey := postJSONStatusWithHeaders(t, router, "/api/rpc/objekPajak/save", payload, http.StatusOK, map[string]string{"X-Admin-API-Key": "secret"})
	if objectAt(t, withKey, "json")["nop"] != "517101000105400330" {
		t.Fatalf("unexpected save response: %v", withKey)
	}

	detail := postJSON(t, router, "/api/rpc/objekPajak/getByNop", map[string]any{
		"kdPropinsi": "51", "kdDati2": "71", "kdKecamatan": "010", "kdKelurahan": "001",
		"kdBlok": "054", "noUrut": "0033", "kdJnsOp": "0",
	})
	if objectAt(t, detail, "json")["nmWp"] != "WP ADMIN" {
		t.Fatalf("saved object was not readable: %v", detail)
	}

	badPayload := validSavePayload("0034", "WP BAD REGION")
	spop := badPayload["spop"].(map[string]any)
	spop["kdBlok"] = "999"
	postJSONStatusWithHeaders(t, router, "/api/rpc/objekPajak/save", badPayload, http.StatusInternalServerError, map[string]string{"X-Admin-API-Key": "secret"})

	missing := postJSON(t, router, "/api/rpc/objekPajak/getByNop", map[string]any{
		"kdPropinsi": "51", "kdDati2": "71", "kdKecamatan": "010", "kdKelurahan": "001",
		"kdBlok": "999", "noUrut": "0034", "kdJnsOp": "0",
	})
	if missing["json"] != nil {
		t.Fatalf("failed transaction should not leave object behind: %v", missing)
	}
}

func TestRESTReadOnlyContract(t *testing.T) {
	router := newFunctionalRouter(t)

	search := getJSON(t, router, "/api/v1/objek-pajak/search?query=51.71.010.001.054.0032.0&limit=5", http.StatusOK)
	assertArrayLenAtLeast(t, search, 1)

	detail := getJSON(t, router, "/api/v1/objek-pajak/51.71.010.001.054.0032.0", http.StatusOK)
	if detail.(map[string]any)["nmWp"] != "I MADE BUDIARTA" {
		t.Fatalf("unexpected REST detail: %v", detail)
	}

	sppt := getJSON(t, router, "/api/v1/sppt/51.71.010.001.054.0032.0/2026", http.StatusOK)
	if intFromJSON(sppt.(map[string]any)["thnPajakSppt"]) != 2026 {
		t.Fatalf("unexpected REST SPPT: %v", sppt)
	}

	facilities := getJSON(t, router, "/api/v1/objek-pajak/51.71.010.001.054.0032.0/bangunan/1/fasilitas", http.StatusOK)
	assertArrayLenAtLeast(t, facilities, 3)

	getJSON(t, router, "/api/v1/objek-pajak/invalid", http.StatusBadRequest)
	assertStatus(t, router, http.MethodPost, "/api/v1/objek-pajak", []byte(`{}`), http.StatusMethodNotAllowed)
}

func TestConcurrentSaveKeepsSingleRowPerNOP(t *testing.T) {
	router, database := newFunctionalRouterWithOptions(t, api.Options{AllowPublicSave: true, MaxBodyBytes: 1_048_576})

	const workers = 20
	var wg sync.WaitGroup
	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			payload := validSavePayload("0035", fmt.Sprintf("WP RACE %02d", i))
			postJSONStatus(t, router, "/api/rpc/objekPajak/save", payload, http.StatusOK)
		}(i)
	}
	wg.Wait()

	var count int
	err := database.QueryRow(`
		SELECT COUNT(*) FROM tax_objects
		WHERE kd_propinsi='51' AND kd_dati2='71' AND kd_kecamatan='010' AND kd_kelurahan='001'
		  AND kd_blok='054' AND no_urut='0035' AND kd_jns_op='0'
	`).Scan(&count)
	if err != nil {
		t.Fatalf("count saved object: %v", err)
	}
	if count != 1 {
		t.Fatalf("concurrent upsert row count = %d, want 1", count)
	}
}

func postJSONStatus(t *testing.T, handler http.Handler, path string, payload map[string]any, expected int) map[string]any {
	t.Helper()
	return postJSONStatusWithHeaders(t, handler, path, payload, expected, nil)
}

func postJSONStatusWithHeaders(t *testing.T, handler http.Handler, path string, payload map[string]any, expected int, headers map[string]string) map[string]any {
	t.Helper()
	body, err := json.Marshal(map[string]any{"json": payload})
	if err != nil {
		t.Fatalf("marshal request: %v", err)
	}
	response := httptestRequest(handler, http.MethodPost, path, body, "application/json", headers)
	if response.Code != expected {
		t.Fatalf("POST %s status = %d, want %d; body=%s", path, response.Code, expected, response.Body.String())
	}
	var decoded map[string]any
	if err := json.Unmarshal(response.Body.Bytes(), &decoded); err != nil {
		t.Fatalf("decode response: %v\n%s", err, response.Body.String())
	}
	return decoded
}

func getJSON(t *testing.T, handler http.Handler, path string, expected int) any {
	t.Helper()
	response := httptestRequest(handler, http.MethodGet, path, nil, "", nil)
	if response.Code != expected {
		t.Fatalf("GET %s status = %d, want %d; body=%s", path, response.Code, expected, response.Body.String())
	}
	var decoded any
	if err := json.Unmarshal(response.Body.Bytes(), &decoded); err != nil {
		t.Fatalf("decode response: %v\n%s", err, response.Body.String())
	}
	return decoded
}

func httptestRequest(handler http.Handler, method, path string, body []byte, contentType string, headers map[string]string) *httptest.ResponseRecorder {
	request := httptest.NewRequest(method, path, bytes.NewReader(body))
	if contentType != "" {
		request.Header.Set("Content-Type", contentType)
	}
	for key, value := range headers {
		request.Header.Set(key, value)
	}
	response := httptest.NewRecorder()
	handler.ServeHTTP(response, request)
	return response
}

func validSavePayload(noUrut string, nmWP string) map[string]any {
	return map[string]any{
		"spop": map[string]any{
			"kdPropinsi": "51", "kdDati2": "71", "kdKecamatan": "010", "kdKelurahan": "001",
			"kdBlok": "054", "noUrut": noUrut, "kdJnsOp": "0", "subjekPajakId": "SUBJ-" + noUrut,
			"jalanOp": "JL. TRANSAKSI TEST", "luasBumi": 100, "nilaiSistemBumi": 200000000,
			"njopBumi": 200000000, "kdStatusWp": "1", "jnsBumi": "1", "jnsTransaksiOp": "1",
		},
		"subjekPajak": map[string]any{
			"subjekPajakId": "SUBJ-" + noUrut,
			"nmWp":          nmWP,
			"jalanWp":       "JL. WP TEST",
		},
	}
}
