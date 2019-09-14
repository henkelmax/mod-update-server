# Update Server

![Pipeline](https://badges.maxhenkel.de/pipeline/14278390) ![Latest](https://badges.maxhenkel.de/tag/14278390)

## Requests

| Method   | Path                         | Description                                                                        |
| -------- | ---------------------------- | ---------------------------------------------------------------------------------- |
| `GET`    | `/mods`                      | List all mods.                                                                     |
| `GET`    | `/mods/MODNAME`              | Get a specific mod.                                                                |
| `GET`    | `/updates/MODNAME`           | Show update entries. Optional query parameter: limit for the max update count.     |
| `GET`    | `/updates/MODNAME/UPDATE_ID` | Show update entry (updateid mcversion, version, updatemessage, tags[latest, ...]). |
| `POST`   | `/updates/MODNAME`           | Add a new update. Requires an apikey in header. See [Update](#update).             |
| `DELETE` | `/updates/MODNAME/UPDATE_ID` | Remove an update. Requires an apikey in header.                                    |
| `DELETE` | `/mods/MODNAME`              | Remove a mod. Requires an apikey in header.                                        |
| `POST`   | `/mods/add`                  | Add a new mod. Requires an apikey in header. See [Mod](#mod).                      |
| `POST`   | `/mods/edit/MODNAME`         | Edit an existing mod. Requires an apikey in header. See [Mod](#mod).               |
| `GET`    | `/forge/MODNAME`             | Forge update check format.                                                         |
| `POST`   | `/apikey/add`                | Add a new apikey. See [ApiKey](#apikey).                                           |
| `DELETE` | `/apikey/APIKEY`  | Remove an apikey.                                                                  |

### Update

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

### Mod

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

### ApiKey

```js
{
	"mods": ["examplemod"]                                          	// The mods that this key has access to ("*" for every mod)
}
```

## Environment Variables

| Variable     | Description                                                     | Default Value |
| ------------ | --------------------------------------------------------------- | ------------- |
| `DB_IP`      | The IP of the MongoDB database                                  | `localhost`   |
| `DB_PORT`    | The port of the MongoDB database                                | `27017`       |
| `DB_NAME`    | The database name                                               | `updates`     |
| `PORT`       | The webserver port                                              | `8080`        |
| `MASTER_KEY` | The master apiKey (e.g. `62387f34-7678-4737-bfc4-2cb600337541`) |               |
