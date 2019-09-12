// @ts-check
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const { MongoClient, ObjectId } = require('mongodb');
const Joi = require('joi');

(async () => {
  const app = express();

  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(bodyParser.json());
  app.use(cors());

  const dbIp = process.env.DB_IP || 'localhost';
  const dbPort = Number.parseInt(process.env.DB_PORT, 10) || 27017;
  const dbUrl = `mongodb://${dbIp}:${dbPort}`;
  const dbName = process.env.DB_NAME || 'updates';

  const port = Number.parseInt(process.env.PORT, 10) || 8080;

  const modIDSchema = Joi.string()
    .min(1)
    .required();

  const limitSchema = Joi.number()
    .integer()
    .min(1)
    .max(128)
    .default(16);

  const objectIDSchema = Joi.string().regex(/[0-9a-fA-F]{24}/);

  const modSchema = Joi.object().keys({
    modID: Joi.string()
      .min(1)
      .required(),
    name: Joi.string()
      .min(1)
      .required(),
    description: Joi.string()
      .min(1)
      .required(),
    websiteURL: Joi.string()
      .min(1)
      .required(),
    downloadURL: Joi.string()
      .min(1)
      .required(),
    issueURL: Joi.string()
      .min(1)
      .required()
  });

  const updateSchema = Joi.object().keys({
    publishDate: Joi.date().iso(),
    gameVersion: Joi.string()
      .min(1)
      .required(),
    version: Joi.string()
      .min(1)
      .required(),
    updateMessages: Joi.array()
      .items(Joi.string())
      .required(),
    releaseType: Joi.string()
      .valid('alpha', 'beta', 'release')
      .default('release'),
    tags: Joi.array()
      .items(Joi.string())
      .required()
  });

  const client = await MongoClient.connect(dbUrl, {
    useNewUrlParser: true,
    useUnifiedTopology: true
  });

  const db = client.db(dbName);

  // Get a specific mod
  app.get('/mods/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const mod = await db.collection('mods').findOne({ modID: modIDElement.value });
    res.status(200).send(mod);
  });

  // Get all mods
  app.get('/mods', async (req, res) => {
    res.status(200).send(
      await db
        .collection('mods')
        .find({})
        .toArray()
    );
  });

  // Get a specific update for a mod
  app.get('/updates/:modID/:updateID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const updateIDElement = objectIDSchema.validate(req.params.updateID);
    if (updateIDElement.error) {
      res.status(400).send({ err: updateIDElement.error.details });
      res.end();
      return;
    }

    const limitElement = limitSchema.validate(req.query.limit);
    if (limitElement.error) {
      res.status(400).send({ err: limitElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: 'Mod does not exist' });
      return;
    }

    const mod = await modCursor.next();

    const updateCursor = db.collection('updates').find({ _id: new ObjectId(updateIDElement.value), mod: mod._id });
    if (!(await updateCursor.hasNext())) {
      res.status(400).send({ err: 'Update does not exist' });
      return;
    }

    const update = await updateCursor.next();

    res.status(200).send(update);
  });

  // Get the updates for a mod
  app.get('/updates/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const limitElement = limitSchema.validate(req.query.limit);
    if (limitElement.error) {
      res.status(400).send({ err: limitElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: 'Mod does not exist' });
      return;
    }

    const mod = await modCursor.next();

    const updates = await db
      .collection('updates')
      .find({ mod: mod._id })
      .sort({ publishDate: -1 })
      .limit(limitElement.value)
      .toArray();
    res.status(200).send(updates);
  });

  // Add an update
  app.post('/updates/:modID', async (req, res) => {
    const element = updateSchema.validate(req.body);
    if (element.error) {
      res.status(400).send({ err: element.error.details });
      res.end();
      return;
    }
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }
    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    const modExists = await modCursor.hasNext();
    if (!modExists) {
      res.status(400).send({ err: 'Mod does not exist' });
      return;
    }
    const result = await db.collection('updates').insertOne({
      ...element.value,
      mod: (await modCursor.next())._id
    });
    if (result.insertedCount !== 1) {
      res.status(400).send({ err: 'Unknown Error' });
      return;
    }
    res.status(200).end();
  });

  // Deleta an update
  app.delete('/updates/:modID/:updateID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const updateIDElement = objectIDSchema.validate(req.params.updateID);
    if (updateIDElement.error) {
      res.status(400).send({ err: updateIDElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: 'Mod does not exist' });
      return;
    }

    const mod = await modCursor.next();

    const result = await db.collection('updates').deleteMany({ _id: new ObjectId(updateIDElement.value), mod: mod._id });
    if (result.deletedCount <= 0) {
      res.status(400).send({ err: 'Update does not exist' });
      return;
    }

    res.status(200).end();
  });

  // Deleta a mod
  app.delete('/updates/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const result = await db.collection('mods').deleteMany({ modID: modIDElement.value });

    if (result.deletedCount <= 0) {
      res.status(400).send({ err: 'Mod does not exist' });
      return;
    }

    res.status(200).end();
  });

  // Add a mod
  app.post('/add', async (req, res) => {
    const element = modSchema.validate(req.body);
    if (element.error) {
      res.status(400).send({ err: element.error.details });
      res.end();
      return;
    }
    const exists = await db
      .collection('mods')
      .find({ modID: element.value.modID })
      .hasNext();
    if (exists) {
      res.status(400).send({ err: 'Mod already exists' });
      return;
    }
    const result = await db.collection('mods').insertOne(element.value);
    if (result.insertedCount !== 1) {
      res.status(400).send({ err: 'Unknown Error' });
      return;
    }
    res.status(200).end();
  });

  // Get the forge update format
  app.get('/forge/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: 'Mod does not exist' });
      return;
    }

    const mod = await modCursor.next();

    const forgeFormat = {
      homepage: mod.websiteURL,
      promos: {}
    };

    const updates = await db
      .collection('updates')
      .aggregate([
        {
          $match: {
            mod: mod._id
          }
        },
        {
          $sort: {
            publishDate: -1
          }
        },
        {
          $project: {
            _id: false,
            releaseType: false,
            mod: false,
            publishDate: false
          }
        }
      ])
      .toArray();

    updates.reverse().forEach(update => {
      if (!forgeFormat[update.gameVersion]) {
        forgeFormat[update.gameVersion] = {};
      }
      forgeFormat[update.gameVersion][update.version] = update.updateMessages.join('\n');

      if (update.tags.includes('recommended')) {
        forgeFormat.promos[`${update.gameVersion}-recommended`] = update.version;
      }
      forgeFormat.promos[`${update.gameVersion}-latest`] = update.version;
    });

    res.status(200).send(forgeFormat);
  });

  app.listen(port, () => {
    console.log(`Listening on port ${port}.`);
  });
})();
