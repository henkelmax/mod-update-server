package http

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"update-server-go/database"
)

type httpServer struct {
	db *database.Database
}

func (server *httpServer) respondJSON(w http.ResponseWriter, r *http.Request, payload any) {
	w.Header().Set("Content-Type", "application/json")
	err := json.NewEncoder(w).Encode(payload)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
}

func (server *httpServer) respondError(w http.ResponseWriter, r *http.Request, status int, detail string) {
	w.Header().Set("Content-Type", "application/problem+json")
	problem := ProblemDetails{
		Type:     "about:blank",
		Title:    http.StatusText(status),
		Status:   status,
		Detail:   detail,
		Instance: r.URL.Path,
	}

	err := json.NewEncoder(w).Encode(problem)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	w.WriteHeader(status)
}

func (server *httpServer) decodeJson(reader io.Reader, value any) error {
	decoder := json.NewDecoder(reader)
	decoder.DisallowUnknownFields()
	return decoder.Decode(&value)
}

type ProblemDetails struct {
	Type     string `json:"type,omitempty"`
	Title    string `json:"title,omitempty"`
	Status   int    `json:"status,omitempty"`
	Detail   string `json:"detail,omitempty"`
	Instance string `json:"instance,omitempty"`
}

func RunHttpServer(db *database.Database, port int) error {
	mux := http.NewServeMux()

	httpServer := httpServer{db: db}

	mux.HandleFunc("GET /mods", httpServer.handleGetMods)
	mux.Handle("POST /mods/add", chainMiddlewares(http.HandlerFunc(httpServer.handleAddMod), httpServer.masterKeyMiddleware))
	mux.Handle("POST /mods/edit/{modID}", chainMiddlewares(http.HandlerFunc(httpServer.handleEditMod), httpServer.apiKeyMiddleware))
	mux.HandleFunc("GET /mods/{modID}", httpServer.handleGetMod)

	err := http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
	if err != nil {
		return err
	}
	return nil
}

func chainMiddlewares(handler http.Handler, middlewares ...func(http.Handler) http.Handler) http.Handler {
	for i := len(middlewares) - 1; i >= 0; i-- {
		handler = middlewares[i](handler)
	}
	return handler
}
