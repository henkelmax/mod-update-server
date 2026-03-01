package http

import (
	"errors"
	"math"
	"net/http"
	"regexp"
	"strconv"
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

func mapUpdateDto(update database.Update) UpdateWithIdAndModDto {
	return UpdateWithIdAndModDto{
		ID: update.ID,
		UpdateWithModDto: UpdateWithModDto{
			Mod: update.Mod,
			UpdateDto: UpdateDto{
				PublishDate:    &update.PublishDate,
				GameVersion:    update.GameVersion,
				Version:        update.Version,
				UpdateMessages: update.UpdateMessages,
				ReleaseType:    update.ReleaseType,
				Tags:           update.Tags,
			},
		},
	}
}

func mapUpdateDtos(mods []database.Update) []UpdateWithIdAndModDto {
	dtos := make([]UpdateWithIdAndModDto, len(mods))
	for i, m := range mods {
		dtos[i] = mapUpdateDto(m)
	}
	return dtos
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

func (server *httpServer) handleGetUpdates(w http.ResponseWriter, r *http.Request) {
	modId := r.PathValue("modID")
	if modId == "" {
		server.respondError(w, r, http.StatusBadRequest, "mod ID is required")
		return
	}
	amount := getQueryRange(r, "amount", 1, 128, 16)
	page := getQueryRange(r, "page", 0, math.MaxInt, 0)
	mods, err := server.db.GetUpdates(modId, amount, page)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}

	server.respondJSON(w, r, mapUpdateDtos(mods))
}

func (server *httpServer) handleGetUpdate(w http.ResponseWriter, r *http.Request) {
	modId := r.PathValue("modID")
	if modId == "" {
		server.respondError(w, r, http.StatusBadRequest, "mod ID is required")
		return
	}
	updateIdStr := r.PathValue("updateID")
	if updateIdStr == "" {
		server.respondError(w, r, http.StatusBadRequest, "update ID is required")
		return
	}
	updateId, err := strconv.ParseInt(updateIdStr, 10, 64)
	if err != nil {
		server.respondError(w, r, http.StatusBadRequest, "invalid update ID")
		return
	}
	exist, err := server.db.DoesModExist(modId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, "failed to check if mod exists")
		return
	}
	if !exist {
		server.respondError(w, r, http.StatusNotFound, "mod not found")
		return
	}
	exist, err = server.db.DoesUpdateExist(updateId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, "failed to check if update exists")
		return
	}
	if !exist {
		server.respondError(w, r, http.StatusNotFound, "update not found")
		return
	}

	update, err := server.db.GetUpdate(modId, updateId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}

	server.respondJSON(w, r, mapUpdateDto(*update))
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
		server.respondError(w, r, http.StatusConflict, "mod not found")
		return
	}

	err = server.db.AddUpdate(*updateDbObject)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusCreated)
}

func (server *httpServer) handleDeleteUpdate(w http.ResponseWriter, r *http.Request) {
	modId := r.PathValue("modID")
	if modId == "" {
		server.respondError(w, r, http.StatusBadRequest, "mod ID is required")
		return
	}
	updateIdStr := r.PathValue("updateID")
	if updateIdStr == "" {
		server.respondError(w, r, http.StatusBadRequest, "update ID is required")
		return
	}
	updateId, err := strconv.ParseInt(updateIdStr, 10, 64)
	if err != nil {
		server.respondError(w, r, http.StatusBadRequest, "invalid update ID")
		return
	}
	exist, err := server.db.DoesModExist(modId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, "failed to check if mod exists")
		return
	}
	if !exist {
		server.respondError(w, r, http.StatusNotFound, "mod not found")
		return
	}
	exist, err = server.db.DoesUpdateExist(updateId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, "failed to check if update exists")
		return
	}
	if !exist {
		server.respondError(w, r, http.StatusNotFound, "update not found")
		return
	}
	err = server.db.DeleteUpdate(modId, updateId)
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	w.WriteHeader(http.StatusOK)
}

func getQueryRange(r *http.Request, param string, min int, max int, def int) int {
	strValue := r.URL.Query().Get(param)
	if strValue == "" {
		return def
	}
	value, err := strconv.Atoi(strValue)
	if err != nil {
		return def
	}
	if value < min {
		return min
	}
	if value > max {
		return max
	}
	return value
}
