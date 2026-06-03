package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"

	"pbbku-mobile-portal/apps/api/internal/api"
	"pbbku-mobile-portal/apps/api/internal/db"
)

func main() {
	if len(os.Args) < 2 {
		serve(os.Args[1:])
		return
	}

	switch os.Args[1] {
	case "serve":
		serve(os.Args[2:])
	case "migrate":
		runMigrate(os.Args[2:])
	case "seed":
		runSeed(os.Args[2:])
	default:
		fmt.Fprintf(os.Stderr, "unknown command: %s\n", os.Args[1])
		fmt.Fprintln(os.Stderr, "usage: pbbku-api [serve|migrate|seed]")
		os.Exit(2)
	}
}

func serve(args []string) {
	fs := flag.NewFlagSet("serve", flag.ExitOnError)
	addr := fs.String("addr", env("PBBKU_API_ADDR", ":8080"), "HTTP listen address")
	dbDriver := fs.String("db-driver", env("PBBKU_DB_DRIVER", "sqlite"), "database driver: sqlite or postgres")
	dbPath := fs.String("db", env("PBBKU_DB_PATH", "data/pbbku.db"), "SQLite database path")
	databaseURL := fs.String("database-url", env("PBBKU_DATABASE_URL", ""), "PostgreSQL connection URL")
	migrationsPath := fs.String("migrations", env("PBBKU_MIGRATIONS_PATH", "migrations"), "migration directory")
	seedPath := fs.String("seed-file", env("PBBKU_SEED_FILE", "seeds/demo.sql"), "seed SQL file")
	runMigrations := fs.Bool("migrate", true, "run migrations before serving")
	seedIfEmpty := fs.Bool("seed-if-empty", true, "seed demo data when database has no tax objects")
	fs.Parse(args)

	database, err := db.Open(dbConfig(*dbDriver, *dbPath, *databaseURL))
	if err != nil {
		log.Fatal(err)
	}
	defer database.Close()

	if *runMigrations {
		if err := db.Migrate(database, *migrationsPath); err != nil {
			log.Fatal(err)
		}
	}
	if *seedIfEmpty {
		if err := db.SeedIfEmpty(database, *seedPath); err != nil {
			log.Fatal(err)
		}
	}

	server := &http.Server{
		Addr:    *addr,
		Handler: api.NewRouter(database),
	}
	log.Printf("PBB-Ku internal API listening on %s", *addr)
	log.Fatal(server.ListenAndServe())
}

func runMigrate(args []string) {
	fs := flag.NewFlagSet("migrate", flag.ExitOnError)
	dbDriver := fs.String("db-driver", env("PBBKU_DB_DRIVER", "sqlite"), "database driver: sqlite or postgres")
	dbPath := fs.String("db", env("PBBKU_DB_PATH", "data/pbbku.db"), "SQLite database path")
	databaseURL := fs.String("database-url", env("PBBKU_DATABASE_URL", ""), "PostgreSQL connection URL")
	migrationsPath := fs.String("migrations", env("PBBKU_MIGRATIONS_PATH", "migrations"), "migration directory")
	fs.Parse(args)

	database, err := db.Open(dbConfig(*dbDriver, *dbPath, *databaseURL))
	if err != nil {
		log.Fatal(err)
	}
	defer database.Close()

	if err := db.Migrate(database, *migrationsPath); err != nil {
		log.Fatal(err)
	}
	log.Println("migrations applied")
}

func runSeed(args []string) {
	fs := flag.NewFlagSet("seed", flag.ExitOnError)
	dbDriver := fs.String("db-driver", env("PBBKU_DB_DRIVER", "sqlite"), "database driver: sqlite or postgres")
	dbPath := fs.String("db", env("PBBKU_DB_PATH", "data/pbbku.db"), "SQLite database path")
	databaseURL := fs.String("database-url", env("PBBKU_DATABASE_URL", ""), "PostgreSQL connection URL")
	seedPath := fs.String("seed-file", env("PBBKU_SEED_FILE", "seeds/demo.sql"), "seed SQL file")
	fs.Parse(args)

	database, err := db.Open(dbConfig(*dbDriver, *dbPath, *databaseURL))
	if err != nil {
		log.Fatal(err)
	}
	defer database.Close()

	if err := db.Seed(database, *seedPath); err != nil {
		log.Fatal(err)
	}
	log.Println("seed data applied")
}

func env(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}

func dbConfig(driver, sqlitePath, databaseURL string) db.Config {
	normalized := db.NormalizeDriver(driver)
	if normalized == db.DriverPostgres {
		if databaseURL == "" {
			databaseURL = "postgres://pbbku:pbbku@localhost:5432/pbbku?sslmode=disable"
		}
		return db.Config{Driver: normalized, DSN: databaseURL}
	}
	return db.Config{Driver: normalized, DSN: sqlitePath}
}
