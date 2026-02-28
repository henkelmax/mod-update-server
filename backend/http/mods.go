package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"regexp"
	"strings"
	"update-server-go/database"
)

var modIdRegex = regexp.MustCompile(`^[a-zA-Z_]+$`)

type ModDto struct {
	ModID       string `json:"modID"`
	Name        string `json:"name"`
	Description string `json:"description"`
	WebsiteURL  string `json:"websiteURL"`
	DownloadURL string `json:"downloadURL"`
	IssueURL    string `json:"issueURL"`
}

func (mod *ModDto) Validate() (*database.Mod, error) {
	if !modIdRegex.MatchString(mod.ModID) {
		return nil, errors.New("invalid mod ID format")
	}
	if strings.TrimSpace(mod.Name) == "" {
		return nil, errors.New("mod name cannot be empty")
	}
	return &database.Mod{
		ModID:       mod.ModID,
		Name:        mod.Name,
		Description: mod.Description,
		WebsiteURL:  mod.WebsiteURL,
		DownloadURL: mod.DownloadURL,
		IssueURL:    mod.IssueURL,
	}, nil
}

func mapModDto(mod database.Mod) ModDto {
	return ModDto{
		ModID:       mod.ModID,
		Name:        mod.Name,
		Description: mod.Description,
		WebsiteURL:  mod.WebsiteURL,
		DownloadURL: mod.DownloadURL,
		IssueURL:    mod.IssueURL,
	}
}

func mapModDtos(mods []database.Mod) []ModDto {
	dtos := make([]ModDto, len(mods))
	for i, m := range mods {
		dtos[i] = mapModDto(m)
	}
	return dtos
}

func (server *httpServer) handleGetMods(w http.ResponseWriter, r *http.Request) {
	mods, err := server.db.GetAllMods()
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}

	server.respondJSON(w, r, mapModDtos(mods))
}

func (server *httpServer) handleAddMod(w http.ResponseWriter, r *http.Request) {
	var mod ModDto
	err := json.NewDecoder(r.Body).Decode(&mod)
	if err != nil {
		server.respondError(w, r, http.StatusBadRequest, err.Error())
		return
	}
	modDbObject, err := mod.Validate()
	if err != nil {
		server.respondError(w, r, http.StatusBadRequest, err.Error())
		return
	}

	exists, err := server.db.DoesModExist(modDbObject.ModID)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, "mod already exists")
		return
	}

	if exists {
		server.respondError(w, r, http.StatusConflict, "mod already exists")
		return
	}

	err = server.db.AddMod(*modDbObject)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusCreated)
}
