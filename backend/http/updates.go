package http

import (
	"errors"
	"net/http"
	"regexp"
	"time"
	"update-server-go/database"
)

var releaseTypeRegex = regexp.MustCompile(`^alpha|beta|release$`)
var modLoaderRegex = regexp.MustCompile(`^forge|neoforge|fabric|quilt$`)

type UpdateDto struct {
	PublishDate    *time.Time `json:"publishDate,omitempty"`
	GameVersion    string     `json:"gameVersion"`
	Version        string     `json:"version"`
	UpdateMessages []string   `json:"updateMessages"`
	ReleaseType    string     `json:"releaseType"`
	Tags           []string   `json:"tags"`
	ModLoader      string     `json:"modLoader"`
}
type UpdateWithModDto struct {
	Mod string `json:"mod"`
	UpdateDto
}

type UpdateWithIdAndModDto struct {
	ID int64 `json:"_id"`
	UpdateWithModDto
}

func (update *UpdateDto) Validate(modId string) (*database.Update, error) {
	publishDate := update.PublishDate
	if publishDate == nil {
		publishDate = new(time.Now())
	}
	if update.GameVersion == "" {
		return nil, errors.New("game version cannot be empty")
	}
	if update.Version == "" {
		return nil, errors.New("version cannot be empty")
	}
	if !releaseTypeRegex.MatchString(update.ReleaseType) {
		return nil, errors.New("release type must be alpha, beta, or release")
	}
	if !modLoaderRegex.MatchString(update.ModLoader) {
		return nil, errors.New("mod loader must be forge, neoforge, fabric, or quilt")
	}

	return &database.Update{
		Mod:            modId,
		PublishDate:    *publishDate,
		GameVersion:    update.GameVersion,
		Version:        update.Version,
		UpdateMessages: update.UpdateMessages,
		ReleaseType:    update.ReleaseType,
		Tags:           update.Tags,
		ModLoader:      update.ModLoader,
	}, nil
}

func (server *httpServer) handleAddUpdate(w http.ResponseWriter, r *http.Request) {
	modId := r.PathValue("modID")
	if modId == "" {
		server.respondError(w, r, http.StatusBadRequest, "mod ID is required")
		return
	}
	var update UpdateDto
	err := server.decodeJson(r.Body, &update)
	if err != nil {
		server.respondError(w, r, http.StatusBadRequest, err.Error())
		return
	}
	updateDbObject, err := update.Validate(modId)
	if err != nil {
		server.respondError(w, r, http.StatusBadRequest, err.Error())
		return
	}

	exists, err := server.db.DoesModExist(modId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, "failed to check if mod exists")
		return
	}

	if !exists {
		server.respondError(w, r, http.StatusConflict, "mod does not exist")
		return
	}

	err = server.db.AddUpdate(*updateDbObject)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusCreated)
}
