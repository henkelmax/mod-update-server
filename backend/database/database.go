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
	UpdateMessages StringArray
	ReleaseType    string
	Tags           StringArray
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
		return fmt.Errorf("failed to update mod: %d rows affected", affected)
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

func (db *Database) GetUpdateCount(modId string) (int64, error) {
	var count int64
	err := db.db.QueryRow("SELECT COUNT(*) FROM updates WHERE mod_id = ?;", modId).Scan(&count)
	if err != nil {
		return 0, err
	}
	return count, nil
}

func (db *Database) DeleteMod(modId string) error {
	rows, err := db.db.Exec("DELETE FROM mods WHERE mod_id = ?;", modId)
	if err != nil {
		return err
	}
	affected, err := rows.RowsAffected()
	if err != nil {
		return err
	}
	if affected < 1 {
		return fmt.Errorf("failed to delete mod from database: %d rows affected", affected)
	}
	return nil
}

func (db *Database) AddUpdate(update Update) error {
	updateMessages, err := update.UpdateMessages.Value()
	if err != nil {
		return err
	}
	tags, err := update.Tags.Value()
	if err != nil {
		return err
	}
	exec, err := db.db.Exec(`
INSERT INTO updates (mod_id, publish_date, game_version, version, update_messages, release_type, tags, mod_loader) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
`, update.Mod, update.PublishDate, update.GameVersion, update.Version, updateMessages, update.ReleaseType, tags, update.ModLoader)
	if err != nil {
		return err
	}
	affected, err := exec.RowsAffected()
	if err != nil {
		return err
	}
	if affected != 1 {
		return fmt.Errorf("failed to add update to database: %d rows affected", affected)
	}
	return nil
}

func (db *Database) GetAllUpdates(amount int, page int) ([]Update, error) {
	rows, err := db.db.Query(`
SELECT id, mod_id, publish_date, game_version, version, update_messages, release_type, tags, mod_loader FROM updates ORDER BY publish_date DESC LIMIT ? OFFSET ?;
`, amount, page*amount)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	return db.mapUpdates(rows)
}

func (db *Database) GetUpdates(modId string, amount int, page int) ([]Update, error) {
	rows, err := db.db.Query(`
SELECT id, mod_id, publish_date, game_version, version, update_messages, release_type, tags, mod_loader FROM updates WHERE mod_id = ? ORDER BY publish_date DESC LIMIT ? OFFSET ?;
`, modId, amount, page*amount)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	return db.mapUpdates(rows)
}

func (db *Database) mapUpdates(rows *sql.Rows) ([]Update, error) {
	var updates []Update

	for rows.Next() {
		var update Update
		err := rows.Scan(&update.ID, &update.Mod, &update.PublishDate, &update.GameVersion, &update.Version, &update.UpdateMessages, &update.ReleaseType, &update.Tags, &update.ModLoader)
		if err != nil {
			return nil, err
		}
		updates = append(updates, update)
	}

	err := rows.Err()
	if err != nil {
		return nil, err
	}

	return updates, nil
}

func (db *Database) GetUpdate(modId string, updateId int64) (*Update, error) {
	var update Update
	err := db.db.QueryRow(`
SELECT id, mod_id, publish_date, game_version, version, update_messages, release_type, tags, mod_loader FROM updates WHERE id = ? AND mod_id = ?;
`, updateId, modId).Scan(&update.ID, &update.Mod, &update.PublishDate, &update.GameVersion, &update.Version, &update.UpdateMessages, &update.ReleaseType, &update.Tags, &update.ModLoader)
	if err != nil {
		return nil, err
	}
	return &update, nil
}

func (db *Database) DoesUpdateExist(id int64) (bool, error) {
	var exists bool
	err := db.db.QueryRow(`SELECT EXISTS(SELECT 1 FROM updates WHERE id = ?)`, id).Scan(&exists)
	if err != nil {
		return false, err

	}
	return exists, nil
}

func (db *Database) DeleteUpdate(modId string, updateId int64) error {
	rows, err := db.db.Exec("DELETE FROM updates WHERE id = ? AND mod_id = ?;", updateId, modId)
	if err != nil {
		return err
	}
	affected, err := rows.RowsAffected()
	if err != nil {
		return err
	}
	if affected < 1 {
		return fmt.Errorf("failed to delete update from database: %d rows affected", affected)
	}
	return nil
}

func (db *Database) UpdateUpdate(update Update) error {
	updateMessages, err := update.UpdateMessages.Value()
	if err != nil {
		return err
	}
	tags, err := update.Tags.Value()
	if err != nil {
		return err
	}
	exec, err := db.db.Exec(`
UPDATE updates SET publish_date = ?, game_version = ?, version = ?, update_messages = ?, release_type = ?, tags = ?, mod_loader = ? WHERE id = ? and mod_id = ?;
`, update.PublishDate, update.GameVersion, update.Version, updateMessages, update.ReleaseType, tags, update.ModLoader, update.ID, update.Mod)
	if err != nil {
		return err
	}
	affected, err := exec.RowsAffected()
	if err != nil {
		return err
	}
	if affected != 1 {
		return fmt.Errorf("failed to update update: %d rows affected", affected)
	}
	return nil
}

func (db *Database) Close() error {
	return db.db.Close()
}
