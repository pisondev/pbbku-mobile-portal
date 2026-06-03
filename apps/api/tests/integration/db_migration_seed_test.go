package integration_test

import (
	"os"
	"path/filepath"
	"runtime"
	"testing"

	"pbbku-mobile-portal/apps/api/internal/db"
)

func TestSQLiteMigrateSeedAndIdempotency(t *testing.T) {
	database := openSQLiteFixture(t)
	defer database.Close()

	var objectCount int
	if err := database.QueryRow("SELECT COUNT(*) FROM tax_objects").Scan(&objectCount); err != nil {
		t.Fatalf("count tax_objects: %v", err)
	}
	if objectCount != 3 {
		t.Fatalf("tax object count = %d, want 3", objectCount)
	}

	if err := db.SeedIfEmpty(database, seedPath(t)); err != nil {
		t.Fatalf("SeedIfEmpty should be idempotent: %v", err)
	}
	if err := database.QueryRow("SELECT COUNT(*) FROM tax_objects").Scan(&objectCount); err != nil {
		t.Fatalf("count tax_objects after idempotent seed: %v", err)
	}
	if objectCount != 3 {
		t.Fatalf("tax object count after idempotent seed = %d, want 3", objectCount)
	}
}

func TestPostgresMigrateSeedWhenConfigured(t *testing.T) {
	dsn := testPostgresURL()
	if dsn == "" {
		t.Skip("set PBBKU_TEST_POSTGRES_URL to run PostgreSQL integration test")
	}

	database, err := db.Open(db.Config{Driver: db.DriverPostgres, DSN: dsn})
	if err != nil {
		t.Fatalf("open postgres: %v", err)
	}
	defer database.Close()

	if err := db.Migrate(database, migrationsPath(t)); err != nil {
		t.Fatalf("migrate postgres: %v", err)
	}
	if err := db.SeedIfEmpty(database, seedPath(t)); err != nil {
		t.Fatalf("seed postgres: %v", err)
	}

	var provinceCount int
	if err := database.QueryRow("SELECT COUNT(*) FROM propinsi").Scan(&provinceCount); err != nil {
		t.Fatalf("count propinsi: %v", err)
	}
	if provinceCount < 2 {
		t.Fatalf("province count = %d, want at least 2", provinceCount)
	}
}

func openSQLiteFixture(t *testing.T) *db.Database {
	t.Helper()
	database, err := db.Open(db.Config{
		Driver: db.DriverSQLite,
		DSN:    filepath.Join(t.TempDir(), "pbbku-test.db"),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.Migrate(database, migrationsPath(t)); err != nil {
		database.Close()
		t.Fatalf("migrate sqlite: %v", err)
	}
	if err := db.SeedIfEmpty(database, seedPath(t)); err != nil {
		database.Close()
		t.Fatalf("seed sqlite: %v", err)
	}
	return database
}

func testPostgresURL() string {
	return getenv("PBBKU_TEST_POSTGRES_URL")
}

func migrationsPath(t *testing.T) string {
	t.Helper()
	return filepath.Join(moduleRoot(t), "migrations")
}

func seedPath(t *testing.T) string {
	t.Helper()
	return filepath.Join(moduleRoot(t), "seeds", "demo.sql")
}

func moduleRoot(t *testing.T) string {
	t.Helper()
	_, file, _, ok := runtime.Caller(0)
	if !ok {
		t.Fatal("cannot resolve test path")
	}
	return filepath.Clean(filepath.Join(filepath.Dir(file), "..", ".."))
}

func getenv(key string) string {
	return os.Getenv(key)
}
