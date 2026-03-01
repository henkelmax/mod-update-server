package http

import (
	"net/http"
	"slices"
	"update-server-go/utils"
)

var masterKey = utils.GetEnv("MASTER_KEY", "00000000-0000-0000-0000-000000000000")

func (server *httpServer) masterKeyMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		apiKey := r.Header.Get("apikey")

		if apiKey != masterKey {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}

		next.ServeHTTP(w, r)
	})
}

func (server *httpServer) apiKeyMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		apiKey := r.Header.Get("apikey")
		modId := r.PathValue("modID")
		if apiKey == "" {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
		if apiKey == masterKey {
			next.ServeHTTP(w, r)
			return
		}

		mods, err := server.db.GetAuthorizedMods(apiKey)
		if err != nil {
			server.respondError(w, r, http.StatusInternalServerError, err.Error())
			return
		}

		if modId == "" {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}

		if !slices.Contains(mods, modId) {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}

		next.ServeHTTP(w, r)
	})
}
