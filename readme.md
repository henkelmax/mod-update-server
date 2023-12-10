# Mod Update Server

A server for Minecraft modders that manages all your mod updates.
It provides a web interface and REST API to manage everything.
This can be used directly for the [Forge Update Checker](https://docs.minecraftforge.net/en/1.20.x/misc/updatechecker/).

## Useful Links

- [Mod Update Gradle Plugin](https://github.com/henkelmax/mod-update-plugin)
- [Forge Update Checker](https://docs.minecraftforge.net/en/1.20.x/misc/updatechecker/)

---

## Requests

| Method   | Path                                | Description                                                                                              |
|----------|-------------------------------------|----------------------------------------------------------------------------------------------------------|
| `GET`    | `/mods`                             | A list of all mods.                                                                                      |
| `GET`    | `/mods/MODNAME`                     | A specific mod by its mod ID.                                                                            |
| `GET`    | `/updates?amount=16&page=0`         | All updates for all mods. Query parameters: `amount` for the update count per page, `page` for the page. |
| `GET`    | `/updates/MODNAME?amount=16&page=0` | All updates for a mod. Query parameters: `amount` for the update count per page, `page` for the page.    |
| `GET`    | `/updates/MODNAME/UPDATE_ID`        | A specific update.                                                                                       |
| `POST`   | `/updates/MODNAME`                  | Adds a new update. Requires an apikey in the header. See [Update](#update).                              |
| `POST`   | `/updates/MODNAME/UPDATE_ID`        | Updates an update.                                                                                       |
| `DELETE` | `/updates/MODNAME/UPDATE_ID`        | Deletes an update. Requires an apikey in the header.                                                     |
| `DELETE` | `/mods/MODNAME`                     | Deletes a mod. Requires an apikey in the header.                                                         |
| `POST`   | `/mods/add`                         | Adds a new mod. Requires an apikey in the header. See [Mod](#mod).                                       |
| `POST`   | `/mods/edit/MODNAME`                | Edits an existing mod. Requires an apikey in the header. See [Mod](#mod).                                |
| `GET`    | `/forge/MODNAME`                    | The Forge update check format.                                                                           |
| `GET`    | `/check/LOADER/MODNAME`             | A general purpose update check format.                                                                   |
| `GET`    | `/apikeys`                          | A list of all API keys.                                                                                  |
| `POST`   | `/apikeys/add`                      | Adds a new API keys. See [ApiKey](#apikey).                                                              |
| `DELETE` | `/apikeys/APIKEY`                   | Removes an API keys.                                                                                     |

**Example Update**

```json5
{
  "publishDate": "2023-12-08T14:48:00",
  // The publishing date (used to order the updates).
  "gameVersion": "1.20.4",
  // The game version.
  "modLoader": "forge",
  // The mod loader [forge, neoforge, fabric, quilt]. Default value: "forge".
  "version": "1.0.0",
  // The mod version.
  "updateMessages": [
    "Updated to 1.20.4",
    "Added readme.md"
  ],
  // The update messages (Changelog etc.).
  "releaseType": "release",
  // The release type [alpha, beta, release]. Default value: "release".
  "tags": [
    "recommended"
  ]
  // Additional tags e.g. recommended.
}
```

**Example Mod**

```json5
{
  "modID": "examplemod",
  // The mod ID (used to identify the mod)
  "name": "Example Mod",
  // The name of the mod
  "description": "Just an example mod",
  // The mod description
  "websiteURL": "https://example.com/examplemod",
  // The URL to the mods website
  "downloadURL": "https://example.com/examplemod/files",
  // The URL to the mods download page
  "issueURL": "https://example.com/examplemod/issues"
  // The issue tracker url of this mod
}
```

**Example ApiKey**

```json5
{
  "mods": [
    "examplemod"
  ]
  // The mods that this key has access to ("*" for every mod)
}
```

## Usage

See [this](docker_compose.md).

## Development Setup

### Prerequisites

- [Java 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Node.js](https://nodejs.org/)
- [Yarn](https://yarnpkg.com/)

### Installation

```sh
./gradlew init
./gradlew frontend:yarn
```

### Running the project in development

```sh
./gradlew backend:bootRun
./gradlew frontend:dev
```

The REST API should be available at port 8088
and the web interface is usually available at port 3000 (If not already in use).

## Environment Variables

| Variable         | Description                                                     | Default Value |
|------------------|-----------------------------------------------------------------|---------------|
| `DB_IP`          | The IP of the PostgreSQL database                               | `localhost`   |
| `DB_PORT`        | The port of the PostgreSQL database                             | `5432`        |
| `DB_NAME`        | The database name                                               | `postgres`    |
| `DB_USER`        | The database username                                           | `postgres`    |
| `DB_PASSWORD`    | The database password                                           | ` `           |
| `PORT`           | The webserver port                                              | `8088`        |
| `MASTER_KEY`     | The master apiKey (e.g. `62387f34-7678-4737-bfc4-2cb600337541`) | ` `           |
| `LOGIN_USERNAME` | The username for the web UI login                               | `admin`       |
| `LOGIN_PASSWORD` | The password for the web UI login                               | `admin`       |
