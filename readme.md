# Mod Update Server ![GitHub release (latest by date)](https://img.shields.io/github/v/release/henkelmax/mod-update-server?include_prereleases)

A server for Minecraft modders that manages all your mod updates.
It provides a web interface and REST API to manage everything.
This can be used directly for the [Forge Update Checker](https://docs.minecraftforge.net/en/1.19.2/misc/updatechecker/).

## Useful Links

- [Mod Update Gradle Plugin](https://github.com/henkelmax/mod-update-plugin)
- [Forge Update Checker](https://docs.minecraftforge.net/en/1.19.2/misc/updatechecker/)

---

## Requests

| Method   | Path                                | Description                                                                                                               |
| -------- |-------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `GET`    | `/mods`                             | List all mods.                                                                                                            |
| `GET`    | `/mods/MODNAME`                     | Get a specific mod.                                                                                                       |
| `GET`    | `/updates?amount=16&page=0`         | Show update entries for all mods. Optional query parameters: `amount` for the update count per page, `page` for the page. |
| `GET`    | `/updates/MODNAME?amount=16&page=0` | Show update entries. Optional query parameters: `amount` for the update count per page, `page` for the page.              |
| `GET`    | `/updates/MODNAME/UPDATE_ID`        | Show update entry (updateid mcversion, version, updatemessage, tags[latest, ...]).                                        |
| `POST`   | `/updates/MODNAME`                  | Add a new update. Requires an apikey in header. See [Update](#update).                                                    |
| `POST`   | `/updates/MODNAME/UPDATE_ID`        | Updates an update.                                                                                                        |
| `DELETE` | `/updates/MODNAME/UPDATE_ID`        | Remove an update. Requires an apikey in header.                                                                           |
| `DELETE` | `/mods/MODNAME`                     | Remove a mod. Requires an apikey in header.                                                                               |
| `POST`   | `/mods/add`                         | Add a new mod. Requires an apikey in header. See [Mod](#mod).                                                             |
| `POST`   | `/mods/edit/MODNAME`                | Edit an existing mod. Requires an apikey in header. See [Mod](#mod).                                                      |
| `GET`    | `/forge/MODNAME`                    | Forge update check format.                                                                                                |
| `GET`    | `/check/LOADER/MODNAME`             | A general purpose update check format.                                                                                    |
| `GET`    | `/latest/MODNAME`                   | General update format.                                                                                                    |
| `GET`    | `/apikeys`                          | List all apikeys.                                                                                                         |
| `POST`   | `/apikeys/add`                      | Add a new apikey. See [ApiKey](#apikey).                                                                                  |
| `DELETE` | `/apikeys/APIKEY`                   | Remove an apikey.                                                                                                         |

**Example Update**

```js
{
  "publishDate": "2019-10-10T14:48:00",                               // The publishing date (used to order the updates).
  "gameVersion": "1.14.4",                                            // The game version.
  "modLoader": "forge",                                               // The mod loader [forge, fabric, quilt]. Default value: "forge".
  "version": "1.0.0",                                                 // The mod version.
  "updateMessages": ["Updated to 1.14.4", "Added readme.md"],         // The update messages (Changelog etc.).
  "releaseType": "release",                                           // The release type [alpha, beta, release]. Default value: "release".
  "tags": ["recommended"]                                             // Additional tags e.g. recommended.
}
```

**Example Mod**

```js
{
  "modID": "examplemod",                                              // The mod ID (used to identify the mod)
  "name": "Example Mod",                                              // The name of the mod
  "description": "Just an example mod",                               // The mod description
  "websiteURL": "https://example.com/examplemod",                     // The URL to the mods website
  "downloadURL": "https://example.com/examplemod/files",              // The URL to the mods download page
  "issueURL": "https://example.com/examplemod/issues"                 // The issue tracker url of this mod
}
```

**Example ApiKey**

```js
{
  "mods": ["examplemod"]                                                // The mods that this key has access to ("*" for every mod)
}
```

## Usage

**Docker Compose**

```yml
version: "3.9"

services:
  update-server:
    build: "https://github.com/henkelmax/mod-update-server.git#2.0.0"
    restart: "always"
    container_name: "update-server"
    environment:
      MASTER_KEY: "b13c2739-f6e4-4123-b486-8f44c3f53bac" # Please generate your own random UUID
      DB_IP: "update-mongodb"
    networks:
      - "update-server"
    depends_on:
      - "update-mongodb"
    ports:
      - "8088:8088" # Ideally, you need a reverse proxy to serve via HTTPS
  update-mongodb:
    image: "mongo:4.4"
    restart: "always"
    container_name: "update-mongodb"
    volumes:
      - "/path/to/your/db/files:/data/db"
    networks:
      - "update-server"
networks:
  update-server:
    internal: true
```

## Development Setup

### Prerequisites

- [Java 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Node.js](https://nodejs.org/)
- [Yarn](https://yarnpkg.com/)
- [MongoDB](https://www.mongodb.com/)

### Installation

```sh
./gradlew init
./gradlew frontend:yarn
```

### Running the project in development

```sh
./gradlew runMongoDB
./gradlew backend:bootRun
./gradlew frontend:serve
```


The REST API should be available at port 8088 and the web interface is usually available at port 8080 (If not already in
use).

## Environment Variables

| Variable         | Description                                                     | Default Value |
| ---------------- | --------------------------------------------------------------- |---------------|
| `DB_IP`          | The IP of the MongoDB database                                  | `localhost`   |
| `DB_PORT`        | The port of the MongoDB database                                | `27017`       |
| `DB_NAME`        | The database name                                               | `updates`     |
| `PORT`           | The webserver port                                              | `8088`        |
| `MASTER_KEY`     | The master apiKey (e.g. `62387f34-7678-4737-bfc4-2cb600337541`) |               |
| `LOGIN_USERNAME` | The username for the web UI login                               | `admin`       |
| `LOGIN_PASSWORD` | The password for the web UI login                               | `admin`       |

