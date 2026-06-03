package unit_test

import (
	"testing"

	"pbbku-mobile-portal/apps/api/internal/db"
)

func TestNormalizeDriver(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		expected string
	}{
		{name: "empty defaults to sqlite", input: "", expected: db.DriverSQLite},
		{name: "sqlite3 alias", input: "sqlite3", expected: db.DriverSQLite},
		{name: "postgresql alias", input: "postgresql", expected: db.DriverPostgres},
		{name: "pg alias", input: "pg", expected: db.DriverPostgres},
		{name: "trim and lowercase", input: "  Postgres  ", expected: db.DriverPostgres},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := db.NormalizeDriver(tt.input); got != tt.expected {
				t.Fatalf("NormalizeDriver(%q) = %q, want %q", tt.input, got, tt.expected)
			}
		})
	}
}

func TestBindPlaceholders(t *testing.T) {
	query := "SELECT * FROM sppt WHERE kd_propinsi = ? AND thn_pajak_sppt = ? LIMIT ?"

	if got := db.BindPlaceholders(query, db.DriverSQLite); got != query {
		t.Fatalf("sqlite placeholders changed: %s", got)
	}

	expected := "SELECT * FROM sppt WHERE kd_propinsi = $1 AND thn_pajak_sppt = $2 LIMIT $3"
	if got := db.BindPlaceholders(query, db.DriverPostgres); got != expected {
		t.Fatalf("postgres placeholders = %q, want %q", got, expected)
	}
}

func TestBindPlaceholdersDoesNotRewriteStringLiterals(t *testing.T) {
	query := "SELECT '?' AS literal, 'it''s ?' AS escaped, kd_propinsi FROM propinsi WHERE kd_propinsi = ?"
	expected := "SELECT '?' AS literal, 'it''s ?' AS escaped, kd_propinsi FROM propinsi WHERE kd_propinsi = $1"

	if got := db.BindPlaceholders(query, db.DriverPostgres); got != expected {
		t.Fatalf("postgres placeholders with literals = %q, want %q", got, expected)
	}
}

func TestConfigFromEnv(t *testing.T) {
	t.Setenv("PBBKU_DB_DRIVER", "postgres")
	t.Setenv("PBBKU_DATABASE_URL", "postgres://user:pass@db:5432/app?sslmode=disable")

	config := db.ConfigFromEnv()
	if config.Driver != db.DriverPostgres {
		t.Fatalf("driver = %q, want postgres", config.Driver)
	}
	if config.DSN != "postgres://user:pass@db:5432/app?sslmode=disable" {
		t.Fatalf("dsn = %q", config.DSN)
	}
}
