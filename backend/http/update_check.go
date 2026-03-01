package http

import (
	"net/http"
)

type UpdateCheckResponseDto struct {
	Homepage *string                         `json:"homepage,omitempty"`
	Versions map[string]VersionUpdateInfoDto `json:"versions"`
}

type VersionUpdateInfoDto struct {
	Latest      VersionDto  `json:"latest"`
	Recommended *VersionDto `json:"recommended,omitempty"`
}

type VersionDto struct {
	Version       string   `json:"version"`
	Changelog     []string `json:"changelog"`
	DownloadLinks []string `json:"downloadLinks,omitempty"`
}

//TODO Implement caching

func (server *httpServer) handleCheckUpdates(w http.ResponseWriter, r *http.Request) {
	loader := r.PathValue("loader")
	if !modLoaderRegex.MatchString(loader) {
		server.respondError(w, r, http.StatusBadRequest, "invalid mod loader")
		return
	}
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

	response := UpdateCheckResponseDto{
		Homepage: &mod.WebsiteURL,
		Versions: map[string]VersionUpdateInfoDto{},
	}

	for _, update := range latestUpdates {
		response.Versions[update.GameVersion] = VersionUpdateInfoDto{
			Latest: VersionDto{
				Version:   update.Version,
				Changelog: update.UpdateMessages,
			},
		}
	}

	for _, update := range recommendedUpdates {
		version, exists := response.Versions[update.GameVersion]
		if !exists {
			version = VersionUpdateInfoDto{}
		}
		version.Recommended = &VersionDto{
			Version:   update.Version,
			Changelog: update.UpdateMessages,
		}
		response.Versions[update.GameVersion] = version
	}

	server.respondJSON(w, r, response)
}
