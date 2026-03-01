package http

import (
	"math"
	"net/http"
	"time"
)

type BackupDto struct {
	BackupDate time.Time           `json:"backupDate"`
	Mods       []ModWithUpdatesDto `json:"mods"`
}

type ModWithUpdatesDto struct {
	ModDto
	Updates []UpdateDto `json:"updates,omitempty"`
}

func (server *httpServer) handleBackup(w http.ResponseWriter, r *http.Request) {
	mods, err := server.db.GetAllMods()
	if err != nil {
		server.respondError(w, r, http.StatusInternalServerError, err.Error())
		return
	}
	backup := BackupDto{
		BackupDate: time.Now(),
	}
	for _, mod := range mods {
		modDto := ModWithUpdatesDto{
			ModDto: ModDto{
				ModID: mod.ModID,
				ModWithoutIdDto: ModWithoutIdDto{
					Name:        mod.Name,
					Description: mod.Description,
					WebsiteURL:  mod.WebsiteURL,
					DownloadURL: mod.DownloadURL,
					IssueURL:    mod.IssueURL,
				},
			},
		}
		updates, err := server.db.GetUpdates(mod.ModID, math.MaxInt, 0)
		if err != nil {
			server.respondError(w, r, http.StatusInternalServerError, err.Error())
			return
		}
		for _, update := range updates {
			modDto.Updates = append(modDto.Updates, UpdateDto{
				PublishDate:    &update.PublishDate,
				GameVersion:    update.GameVersion,
				Version:        update.Version,
				UpdateMessages: update.UpdateMessages,
				ReleaseType:    update.ReleaseType,
				Tags:           update.Tags,
				ModLoader:      update.ModLoader,
			})
		}
		backup.Mods = append(backup.Mods, modDto)
	}
	server.respondJSON(w, r, backup)
}
