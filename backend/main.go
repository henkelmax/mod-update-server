package main

import (
	"fmt"
	"log/slog"
	"update-server-go/database"
	"update-server-go/http"
	"update-server-go/log"
)

func main() {
	log.InitializeLogging()

	db, err := database.InitializeDatabase()
	defer func(db *database.Database) {
		err := db.Close()
		if err != nil {
			slog.Error(fmt.Sprintf("Failed to close database: %s", err.Error()))
		}
	}(db)

	if err != nil {
		slog.Error(fmt.Sprintf("Failed to initialize database: %s", err.Error()))
		return
	}

	// TODO Use environment variable
	port := 8080
	slog.Info(fmt.Sprintf("Running HTTP server on port %d", port))
	err = http.RunHttpServer(db, port)
	if err != nil {
		slog.Error(fmt.Sprintf("Failed to run HTTP server: %s", err.Error()))
		return
	}
}
