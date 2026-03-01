package http

import (
	"net/http"
	"update-server-go/utils"
)

var masterKey = utils.GetEnv("MASTER_KEY", "00000000-0000-0000-0000-000000000000")

func (server *httpServer) apiKeyMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		apiKey := r.Header.Get("apikey")

		if apiKey != masterKey {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}

		next.ServeHTTP(w, r)
	})
}
