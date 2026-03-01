package http

import (
	"fmt"
	"net/http"
	"strings"
)

//TODO Implement caching

func (server *httpServer) handleGetForgeUpdates(w http.ResponseWriter, r *http.Request) {
	server.handleGetForgeUpdatesForLoader(w, r, "forge")
}

func (server *httpServer) handleGetNeoForgeUpdates(w http.ResponseWriter, r *http.Request) {
	server.handleGetForgeUpdatesForLoader(w, r, "neoforge")
}

func (server *httpServer) handleGetForgeUpdatesForLoader(w http.ResponseWriter, r *http.Request, loader string) {
	modId := r.PathValue("modID")
	if modId == "" {
		server.respondError(w, r, http.StatusBadRequest, "mod ID is required")
		return
	}
	exist, err := server.db.DoesModExist(modId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	if !exist {
		server.respondError(w, r, http.StatusNotFound, "mod not found")
		return
	}
	mod, err := server.db.GetMod(modId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}

	latestUpdates, err := server.db.GetLatestUpdates(modId, loader)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	recommendedUpdates, err := server.db.GetRecommendedUpdates(modId, loader)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}

	forgeFormat := map[string]any{}
	promos := map[string]string{}

	for _, update := range latestUpdates {
		value, exists := forgeFormat[update.GameVersion]
		if !exists {
			value = map[string]any{}
		}
		versions := value.(map[string]any)
		versions[update.Version] = strings.Join(update.UpdateMessages, "\n")
		forgeFormat[update.GameVersion] = versions
		promos[fmt.Sprintf("%s-latest", update.GameVersion)] = update.Version
	}

	for _, update := range recommendedUpdates {
		value, exists := forgeFormat[update.GameVersion]
		if !exists {
			value = map[string]any{}
		}
		versions := value.(map[string]any)
		versions[update.Version] = strings.Join(update.UpdateMessages, "\n")
		forgeFormat[update.GameVersion] = versions
		promos[fmt.Sprintf("%s-recommended", update.GameVersion)] = update.Version
	}

	forgeFormat["promos"] = promos
	forgeFormat["homepage"] = mod.WebsiteURL
	server.respondJSON(w, r, forgeFormat)
}
