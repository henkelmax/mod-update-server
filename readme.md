# Mod Update Server ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/henkelmax/mod-update-server/Build) ![GitHub issues](https://img.shields.io/github/issues-raw/henkelmax/mod-update-server) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/henkelmax/mod-update-server?include_prereleases)

A server for Minecraft modders that manages all your mod updates.
It provides a web interface and REST API to manage everything.
This can be used directly for the [Forge Update Checker](https://mcforge.readthedocs.io/en/latest/gettingstarted/autoupdate/).

## Requests

| Method   | Path                         | Description                                                                                 |
| -------- | ---------------------------- | ------------------------------------------------------------------------------------------- |
| `GET`    | `/mods`                      | List all mods.                                                                              |
| `GET`    | `/mods/MODNAME`              | Get a specific mod.                                                                         |
| `GET`    | `/updates`                   | Show update entries for all mods. Optional query parameter: limit for the max update count. |
| `GET`    | `/updates/MODNAME`           | Show update entries. Optional query parameter: limit for the max update count.              |
| `GET`    | `/updates/MODNAME/UPDATE_ID` | Show update entry (updateid mcversion, version, updatemessage, tags[latest, ...]).          |
| `POST`   | `/updates/MODNAME`           | Add a new update. Requires an apikey in header. See [Update](#update).                      |
| `POST`   | `/updates/MODNAME/UPDATE_ID` | Updates an update.                                                                          |
| `DELETE` | `/updates/MODNAME/UPDATE_ID` | Remove an update. Requires an apikey in header.                                             |
| `DELETE` | `/mods/MODNAME`              | Remove a mod. Requires an apikey in header.                                                 |
| `POST`   | `/mods/add`                  | Add a new mod. Requires an apikey in header. See [Mod](#mod).                               |
| `POST`   | `/mods/edit/MODNAME`         | Edit an existing mod. Requires an apikey in header. See [Mod](#mod).                        |
| `GET`    | `/forge/MODNAME`             | Forge update check format.                                                                  |
| `GET`    | `/latest/MODNAME`            | General update format.                                                                      |
| `GET`    | `/apikeys`                   | List all apikeys.                                                                           |
| `POST`   | `/apikeys/add`               | Add a new apikey. See [ApiKey](#apikey).                                                    |
| `DELETE` | `/apikeys/APIKEY`            | Remove an apikey.                                                                           |

**Example Update**

```js
{
  "publishDate": "2019-10-10T14:48:00",                               // The publishing date (used to order the updates).
  "gameVersion": "1.14.4",                                            // The game version.
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

## Development Setup

### Prerequisites

- [Node.js](https://nodejs.org/)
- [Yarn](https://yarnpkg.com/)
- [MongoDB](https://www.mongodb.com/)

### Installation

``` sh
yarn install

cd frontend
yarn install
```

### Running the project in development

``` sh
mongod --dbpath <PATH_TO_DB>

yarn dev

cd frontend
yarn serve
```

The REST API should be available at port 8088 and the web interface is usually available at port 8080 (If not already in use).

## Environment Variables

| Variable         | Description                                                     | Default Value |
| ---------------- | --------------------------------------------------------------- | ------------- |
| `DB_IP`          | The IP of the MongoDB database                                  | `localhost`   |
| `DB_PORT`        | The port of the MongoDB database                                | `27017`       |
| `DB_NAME`        | The database name                                               | `updates`     |
| `PORT`           | The webserver port                                              | `8088`        |
| `MASTER_KEY`     | The master apiKey (e.g. `62387f34-7678-4737-bfc4-2cb600337541`) |               |
| `LOGIN_USERNAME` | The username for the web UI login                               |               |
| `LOGIN_PASSWORD` | The password for the web UI login                               |               |

## Web UI

### Login

You can log in to the web UI via the credentials provided by the environment variables or with the username `apikey` and your apikey as password.
