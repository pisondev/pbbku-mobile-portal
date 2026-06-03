package functional_test

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"path/filepath"
	"runtime"
	"testing"

	"pbbku-mobile-portal/apps/api/internal/api"
	"pbbku-mobile-portal/apps/api/internal/db"
)

func TestPbbKuApiFunctionalFlow(t *testing.T) {
	router := newFunctionalRouter(t)

	assertStatus(t, router, http.MethodGet, "/health", nil, http.StatusOK)

	propinsi := postJSON(t, router, "/api/rpc/wilayah/listPropinsi", map[string]any{})
	assertArrayLenAtLeast(t, propinsi["json"], 2)

	dati2 := postJSON(t, router, "/api/rpc/wilayah/listDati2", map[string]any{"kdPropinsi": "51"})
	assertArrayLenAtLeast(t, dati2["json"], 2)

	kecamatan := postJSON(t, router, "/api/rpc/wilayah/listKecamatan", map[string]any{"kdPropinsi": "51", "kdDati2": "71"})
	assertArrayLenAtLeast(t, kecamatan["json"], 2)

	kelurahan := postJSON(t, router, "/api/rpc/wilayah/listKelurahan", map[string]any{"kdPropinsi": "51", "kdDati2": "71", "kdKecamatan": "010"})
	assertArrayLenAtLeast(t, kelurahan["json"], 2)

	blok := postJSON(t, router, "/api/rpc/wilayah/listBlok", map[string]any{"kdPropinsi": "51", "kdDati2": "71", "kdKecamatan": "010", "kdKelurahan": "001"})
	assertArrayLenAtLeast(t, blok["json"], 2)

	search := postJSON(t, router, "/api/rpc/objekPajak/search", map[string]any{"query": "BUDI", "limit": 5})
	assertArrayLenAtLeast(t, search["json"], 2)

	list := postJSON(t, router, "/api/rpc/objekPajak/listDetails", map[string]any{"kdPropinsi": "51", "kdDati2": "71", "limit": 10, "offset": 0})
	listJSON := objectAt(t, list, "json")
	assertArrayLenAtLeast(t, listJSON["rows"], 2)
	if total := intFromJSON(listJSON["total"]); total != 2 {
		t.Fatalf("list total = %d, want 2", total)
	}

	nop := denpasarNop()
	detail := postJSON(t, router, "/api/rpc/objekPajak/getByNop", nop)
	detailJSON := objectAt(t, detail, "json")
	if detailJSON["nmWp"] != "I MADE BUDIARTA" {
		t.Fatalf("nmWp = %v", detailJSON["nmWp"])
	}

	buildings := postJSON(t, router, "/api/rpc/lspop/listByNop", nop)
	assertArrayLenAtLeast(t, buildings["json"], 1)

	buildingReq := copyMap(nop)
	buildingReq["noBng"] = 1
	building := postJSON(t, router, "/api/rpc/lspop/getBuilding", buildingReq)
	if objectAt(t, building, "json")["jpb"] != "Perumahan" {
		t.Fatalf("unexpected building jpb: %v", building)
	}

	facilities := postJSON(t, router, "/api/rpc/lspop/listFasilitas", buildingReq)
	assertArrayLenAtLeast(t, facilities["json"], 3)

	history := postJSON(t, router, "/api/rpc/sppt/listByNop", nop)
	assertArrayLenAtLeast(t, history["json"], 4)

	spptReq := copyMap(nop)
	spptReq["thnPajakSppt"] = 2026
	sppt := postJSON(t, router, "/api/rpc/sppt/get", spptReq)
	if year := intFromJSON(objectAt(t, sppt, "json")["thnPajakSppt"]); year != 2026 {
		t.Fatalf("sppt year = %d, want 2026", year)
	}

	tunggakan := postJSON(t, router, "/api/rpc/objekPajak/getTunggakan", nop)
	assertArrayLenAtLeast(t, tunggakan["json"], 2)

	nextNoUrut := postJSON(t, router, "/api/rpc/objekPajak/getNextNoUrut", map[string]any{
		"kdPropinsi": "51", "kdDati2": "71", "kdKecamatan": "010", "kdKelurahan": "001", "kdBlok": "054",
	})
	if objectAt(t, nextNoUrut, "json")["noUrut"] != "0033" {
		t.Fatalf("next noUrut response = %v", nextNoUrut)
	}

	nextNoBng := postJSON(t, router, "/api/rpc/lspop/nextNoBng", nop)
	if intFromJSON(objectAt(t, nextNoBng, "json")["noBng"]) != 2 {
		t.Fatalf("next noBng response = %v", nextNoBng)
	}

	spptList := postJSON(t, router, "/api/rpc/sppt/list", map[string]any{"thnPajak": 2026, "kdPropinsi": "51", "statusPembayaran": "0", "limit": 10, "offset": 0})
	assertArrayLenAtLeast(t, objectAt(t, spptList, "json")["rows"], 2)
}

func newFunctionalRouter(t *testing.T) http.Handler {
	t.Helper()
	database := newFunctionalDatabase(t)
	return api.NewRouter(database)
}

func newFunctionalRouterWithOptions(t *testing.T, options api.Options) (http.Handler, *db.Database) {
	t.Helper()
	database := newFunctionalDatabase(t)
	return api.NewRouterWithOptions(database, options), database
}

func newFunctionalDatabase(t *testing.T) *db.Database {
	t.Helper()
	database, err := db.Open(db.Config{Driver: db.DriverSQLite, DSN: filepath.Join(t.TempDir(), "functional.db")})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	t.Cleanup(func() { database.Close() })
	if err := db.Migrate(database, filepath.Join(moduleRoot(t), "migrations")); err != nil {
		t.Fatalf("migrate: %v", err)
	}
	if err := db.SeedIfEmpty(database, filepath.Join(moduleRoot(t), "seeds", "demo.sql")); err != nil {
		t.Fatalf("seed: %v", err)
	}
	return database
}

func postJSON(t *testing.T, handler http.Handler, path string, payload map[string]any) map[string]any {
	t.Helper()
	body, err := json.Marshal(map[string]any{"json": payload})
	if err != nil {
		t.Fatalf("marshal request: %v", err)
	}
	response := assertStatus(t, handler, http.MethodPost, path, body, http.StatusOK)
	var decoded map[string]any
	if err := json.Unmarshal(response.Body.Bytes(), &decoded); err != nil {
		t.Fatalf("decode response %s: %v\n%s", path, err, response.Body.String())
	}
	return decoded
}

func assertStatus(t *testing.T, handler http.Handler, method, path string, body []byte, expected int) *httptest.ResponseRecorder {
	t.Helper()
	request := httptest.NewRequest(method, path, bytes.NewReader(body))
	if body != nil {
		request.Header.Set("Content-Type", "application/json")
	}
	response := httptest.NewRecorder()
	handler.ServeHTTP(response, request)
	if response.Code != expected {
		t.Fatalf("%s %s status = %d, want %d; body=%s", method, path, response.Code, expected, response.Body.String())
	}
	return response
}

func denpasarNop() map[string]any {
	return map[string]any{
		"kdPropinsi": "51", "kdDati2": "71", "kdKecamatan": "010", "kdKelurahan": "001",
		"kdBlok": "054", "noUrut": "0032", "kdJnsOp": "0",
	}
}

func copyMap(input map[string]any) map[string]any {
	output := make(map[string]any, len(input))
	for key, value := range input {
		output[key] = value
	}
	return output
}

func objectAt(t *testing.T, input map[string]any, key string) map[string]any {
	t.Helper()
	value, ok := input[key].(map[string]any)
	if !ok {
		t.Fatalf("%s is not object: %#v", key, input[key])
	}
	return value
}

func assertArrayLenAtLeast(t *testing.T, value any, minimum int) {
	t.Helper()
	array, ok := value.([]any)
	if !ok {
		t.Fatalf("value is not array: %#v", value)
	}
	if len(array) < minimum {
		t.Fatalf("array len = %d, want at least %d; value=%#v", len(array), minimum, value)
	}
}

func intFromJSON(value any) int {
	switch typed := value.(type) {
	case float64:
		return int(typed)
	case int:
		return typed
	default:
		return 0
	}
}

func moduleRoot(t *testing.T) string {
	t.Helper()
	_, file, _, ok := runtime.Caller(0)
	if !ok {
		t.Fatal("cannot resolve test path")
	}
	return filepath.Clean(filepath.Join(filepath.Dir(file), "..", ".."))
}
