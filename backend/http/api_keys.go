package http

import (
	"net/http"
	"update-server-go/database"

	"github.com/google/uuid"
)

type ApiKeyDto struct {
	ApiKey string   `json:"apiKey"`
	Mods   []string `json:"mods"`
}

func mapApiKeyDto(apiKey database.ApiKey) ApiKeyDto {
	return ApiKeyDto{
		ApiKey: apiKey.ApiKey,
		Mods:   apiKey.Mods,
	}
}

func mapApiKeyDtos(apiKeys []database.ApiKey) []ApiKeyDto {
	dtos := make([]ApiKeyDto, len(apiKeys))
	for i, a := range apiKeys {
		dtos[i] = mapApiKeyDto(a)
	}
	return dtos
}

func (server *httpServer) handleGetApiKeys(w http.ResponseWriter, r *http.Request) {
	apiKeys, err := server.db.GetApiKeys()
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}

	server.respondJSON(w, r, mapApiKeyDtos(apiKeys))
}

func (server *httpServer) handleAddApiKey(w http.ResponseWriter, r *http.Request) {
	var mods []string
	err := server.decodeJson(r.Body, &mods)
	if err != nil {
		server.respondError(w, r, http.StatusBadRequest, err.Error())
		return
	}
	for _, mod := range mods {
		if !modIdRegex.MatchString(mod) {
			server.respondError(w, r, http.StatusBadRequest, "invalid mod ID")
		}
	}
	random, err := uuid.NewRandom()
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	apiKey := database.ApiKey{
		ApiKey: random.String(),
		Mods:   mods,
	}
	err = server.db.AddApiKey(apiKey)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusCreated)
	server.respondJSON(w, r, mapApiKeyDto(apiKey))
}

func (server *httpServer) handleDeleteApiKey(w http.ResponseWriter, r *http.Request) {
	apiKey := r.PathValue("apiKey")
	err := server.db.DeleteApiKey(apiKey)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusOK)
}
