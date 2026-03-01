package database

import (
	"database/sql"
	"fmt"
	"time"

	_ "modernc.org/sqlite"
)

type Mod struct {
	ModID       string
	Name        string
	Description string
	WebsiteURL  string
	DownloadURL string
	IssueURL    string
}

type Update struct {
	ID             int64
	Mod            string
	PublishDate    time.Time
	GameVersion    string
	Version        string
	UpdateMessages []string
	ReleaseType    string
	Tags           []string
	ModLoader      string
}

type Database struct {
	db *sql.DB
}

func InitializeDatabase() (*Database, error) {
	db, err := sql.Open("sqlite", "data.db?_pragma=foreign_keys(1)&_pragma=journal_mode(WAL)&_pragma=synchronous(NORMAL)&_pragma=busy_timeout(5000)")
	if err != nil {
		return nil, err
	}
	_, err = db.Exec(`
CREATE TABLE IF NOT EXISTS mods (
    mod_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    website_url TEXT,
    download_url TEXT,
    issue_url TEXT
);

CREATE TABLE IF NOT EXISTS updates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mod_id TEXT NOT NULL,
    publish_date DATETIME,
    game_version TEXT,
    version TEXT,
    update_messages TEXT,
    release_type TEXT,
    tags TEXT,
    mod_loader TEXT,
    FOREIGN KEY(mod_id) REFERENCES mods(mod_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS api_keys (
    api_key TEXT PRIMARY KEY,
    mods TEXT
);
`)
	if err != nil {
		_ = db.Close()
		return nil, err
	}

	return &Database{db: db}, nil
}

func (db *Database) AddMod(mod Mod) error {
	exec, err := db.db.Exec(`
INSERT INTO mods (mod_id, name, description, website_url, download_url, issue_url) VALUES (?, ?, ?, ?, ?, ?)
`, mod.ModID, mod.Name, mod.Description, mod.WebsiteURL, mod.DownloadURL, mod.IssueURL)
	if err != nil {
		return err
	}
	affected, err := exec.RowsAffected()
	if err != nil {
		return err
	}
	if affected != 1 {
		return fmt.Errorf("failed to add mod to database: %d rows affected", affected)
	}
	return nil
}

func (db *Database) GetAllMods() ([]Mod, error) {
	rows, err := db.db.Query("SELECT mod_id, name, description, website_url, download_url, issue_url FROM mods;")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var mods []Mod

	for rows.Next() {
		var mod Mod
		err := rows.Scan(&mod.ModID, &mod.Name, &mod.Description, &mod.WebsiteURL, &mod.DownloadURL, &mod.IssueURL)
		if err != nil {
			return nil, err
		}
		mods = append(mods, mod)
	}

	err = rows.Err()
	if err != nil {
		return nil, err
	}

	return mods, nil
}

func (db *Database) DoesModExist(modId string) (bool, error) {
	var exists bool
	err := db.db.QueryRow(`SELECT EXISTS(SELECT 1 FROM mods WHERE mod_id = ?)`, modId).Scan(&exists)
	if err != nil {
		return false, err

	}
	return exists, nil
}

func (db *Database) GetAuthorizedMods(apiKey string) ([]string, error) {
	var mods StringArray
	err := db.db.QueryRow("SELECT mods FROM api_keys WHERE api_key = ?;", apiKey).Scan(&mods)
	if err != nil {
		return nil, err
	}
	return mods, nil
}

func (db *Database) UpdateMod(mod Mod) error {
	exec, err := db.db.Exec(`
UPDATE mods SET name = ?, description = ?, website_url = ?, download_url = ?, issue_url = ? WHERE mod_id = ?;
`, mod.Name, mod.Description, mod.WebsiteURL, mod.DownloadURL, mod.IssueURL, mod.ModID)
	if err != nil {
		return err
	}
	affected, err := exec.RowsAffected()
	if err != nil {
		return err
	}
	if affected != 1 {
		return fmt.Errorf("failed to add mod to database: %d rows affected", affected)
	}
	return nil
}

func (db *Database) GetMod(modId string) (*Mod, error) {
	var mod Mod
	err := db.db.QueryRow(`
SELECT mod_id, name, description, website_url, download_url, issue_url FROM mods WHERE mod_id = ?;
`, modId).Scan(&mod.ModID, &mod.Name, &mod.Description, &mod.WebsiteURL, &mod.DownloadURL, &mod.IssueURL)
	if err != nil {
		return nil, err
	}
	return &mod, nil
}

func (db *Database) Close() error {
	return db.db.Close()
}
