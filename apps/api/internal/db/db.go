package db

import (
	"context"
	"database/sql"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"

	_ "github.com/lib/pq"
	_ "github.com/mattn/go-sqlite3"
)

const (
	DriverSQLite   = "sqlite"
	DriverPostgres = "postgres"
)

type Config struct {
	Driver string
	DSN    string
}

type Database struct {
	SQL    *sql.DB
	Driver string
}

type Tx struct {
	tx     *sql.Tx
	driver string
}

func ConfigFromEnv() Config {
	driver := NormalizeDriver(os.Getenv("PBBKU_DB_DRIVER"))
	if driver == "" {
		driver = DriverSQLite
	}
	if driver == DriverPostgres {
		return Config{
			Driver: driver,
			DSN:    env("PBBKU_DATABASE_URL", "postgres://pbbku:pbbku@localhost:5432/pbbku?sslmode=disable"),
		}
	}
	return Config{
		Driver: driver,
		DSN:    env("PBBKU_DB_PATH", "data/pbbku.db"),
	}
}

func NormalizeDriver(driver string) string {
	switch strings.ToLower(strings.TrimSpace(driver)) {
	case "", "sqlite", "sqlite3":
		return DriverSQLite
	case "postgres", "postgresql", "pg":
		return DriverPostgres
	default:
		return strings.ToLower(strings.TrimSpace(driver))
	}
}

func Open(config Config) (*Database, error) {
	driver := NormalizeDriver(config.Driver)
	if driver == "" {
		driver = DriverSQLite
	}
	if config.DSN == "" {
		config = ConfigFromEnv()
		driver = NormalizeDriver(config.Driver)
	}

	if driver == DriverSQLite {
		if dir := filepath.Dir(config.DSN); dir != "." && dir != "" {
			if err := os.MkdirAll(dir, 0755); err != nil {
				return nil, err
			}
		}
	}

	sqlDriver, err := sqlDriverName(driver)
	if err != nil {
		return nil, err
	}
	database, err := sql.Open(sqlDriver, config.DSN)
	if err != nil {
		return nil, err
	}

	conn := &Database{SQL: database, Driver: driver}
	if driver == DriverSQLite {
		if _, err := database.Exec(`
			PRAGMA foreign_keys = ON;
			PRAGMA journal_mode = WAL;
			PRAGMA busy_timeout = 5000;
		`); err != nil {
			database.Close()
			return nil, err
		}
	}
	return conn, nil
}

func (d *Database) Close() error {
	return d.SQL.Close()
}

func (d *Database) PingContext(ctx context.Context) error {
	return d.SQL.PingContext(ctx)
}

func (d *Database) ExecContext(ctx context.Context, query string, args ...any) (sql.Result, error) {
	return d.SQL.ExecContext(ctx, BindPlaceholders(query, d.Driver), args...)
}

func (d *Database) QueryContext(ctx context.Context, query string, args ...any) (*sql.Rows, error) {
	return d.SQL.QueryContext(ctx, BindPlaceholders(query, d.Driver), args...)
}

func (d *Database) QueryRowContext(ctx context.Context, query string, args ...any) *sql.Row {
	return d.SQL.QueryRowContext(ctx, BindPlaceholders(query, d.Driver), args...)
}

func (d *Database) QueryRow(query string, args ...any) *sql.Row {
	return d.SQL.QueryRow(BindPlaceholders(query, d.Driver), args...)
}

func (d *Database) BeginTx(ctx context.Context, opts *sql.TxOptions) (*Tx, error) {
	tx, err := d.SQL.BeginTx(ctx, opts)
	if err != nil {
		return nil, err
	}
	return &Tx{tx: tx, driver: d.Driver}, nil
}

func (t *Tx) ExecContext(ctx context.Context, query string, args ...any) (sql.Result, error) {
	return t.tx.ExecContext(ctx, BindPlaceholders(query, t.driver), args...)
}

func (t *Tx) Commit() error {
	return t.tx.Commit()
}

func (t *Tx) Rollback() error {
	return t.tx.Rollback()
}

func Migrate(database *Database, migrationsPath string) error {
	if _, err := database.ExecContext(context.Background(), `
		CREATE TABLE IF NOT EXISTS schema_migrations (
			version TEXT PRIMARY KEY,
			applied_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
		)
	`); err != nil {
		return err
	}

	entries, err := os.ReadDir(migrationsPath)
	if err != nil {
		return err
	}
	files := make([]string, 0, len(entries))
	for _, entry := range entries {
		if !entry.IsDir() && strings.HasSuffix(entry.Name(), ".sql") {
			files = append(files, entry.Name())
		}
	}
	sort.Strings(files)

	for _, file := range files {
		applied, err := migrationApplied(database, file)
		if err != nil {
			return err
		}
		if applied {
			continue
		}
		sqlText, err := os.ReadFile(filepath.Join(migrationsPath, file))
		if err != nil {
			return err
		}
		tx, err := database.BeginTx(context.Background(), nil)
		if err != nil {
			return err
		}
		if _, err := tx.ExecContext(context.Background(), string(sqlText)); err != nil {
			tx.Rollback()
			return fmt.Errorf("migration %s: %w", file, err)
		}
		if _, err := tx.ExecContext(context.Background(), "INSERT INTO schema_migrations(version) VALUES (?)", file); err != nil {
			tx.Rollback()
			return err
		}
		if err := tx.Commit(); err != nil {
			return err
		}
	}
	return nil
}

func Seed(database *Database, seedPath string) error {
	sqlText, err := os.ReadFile(seedPath)
	if err != nil {
		return err
	}
	_, err = database.ExecContext(context.Background(), string(sqlText))
	return err
}

func SeedIfEmpty(database *Database, seedPath string) error {
	var count int
	if err := database.QueryRow("SELECT COUNT(*) FROM tax_objects").Scan(&count); err != nil {
		return err
	}
	if count > 0 {
		return nil
	}
	return Seed(database, seedPath)
}

func migrationApplied(database *Database, version string) (bool, error) {
	var found int
	err := database.QueryRow("SELECT COUNT(*) FROM schema_migrations WHERE version = ?", version).Scan(&found)
	return found > 0, err
}

func BindPlaceholders(query string, driver string) string {
	if NormalizeDriver(driver) != DriverPostgres {
		return query
	}
	var out strings.Builder
	index := 1
	inSingleQuote := false
	for i := 0; i < len(query); i++ {
		char := rune(query[i])
		if char == '\'' {
			out.WriteRune(char)
			if inSingleQuote && i+1 < len(query) && query[i+1] == '\'' {
				i++
				out.WriteByte(query[i])
				continue
			}
			inSingleQuote = !inSingleQuote
			continue
		}
		if char == '?' {
			if inSingleQuote {
				out.WriteRune(char)
				continue
			}
			out.WriteString(fmt.Sprintf("$%d", index))
			index++
			continue
		}
		out.WriteRune(char)
	}
	return out.String()
}

func sqlDriverName(driver string) (string, error) {
	switch NormalizeDriver(driver) {
	case DriverSQLite:
		return "sqlite3", nil
	case DriverPostgres:
		return "postgres", nil
	default:
		return "", fmt.Errorf("unsupported database driver: %s", driver)
	}
}

func env(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}
