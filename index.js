// @ts-check
const express = require('express');
const apicache = require('apicache');
const bodyParser = require('body-parser');
const cors = require('cors');
const { MongoClient, ObjectId } = require('mongodb');
const Joi = require('joi');
const { v4: uuid } = require('uuid');
const path = require('path');
const auth = require('basic-auth');

const cache = apicache.middleware;

const modIDSchema = Joi.string()
  .min(1)
  .regex(/[a-z-_]*/)
  .required();

const limitSchema = Joi.number()
  .integer()
  .min(1)
  .max(128)
  .default(16);

const objectIDSchema = Joi.string().regex(/[0-9a-fA-F]{24}/);

const apiKeySchema = Joi.string().regex(/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/);

const modSchema = Joi.object().keys({
  modID: Joi.string()
    .regex(/[a-z]*/)
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

const modUpdateSchema = Joi.object().keys({
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
    .items(Joi.string().allow(''))
    .required(),
  releaseType: Joi.string()
    .valid('alpha', 'beta', 'release')
    .default('release'),
  tags: Joi.array()
    .items(Joi.string())
    .required()
});

const apiKeyModsSchema = Joi.object().keys({
  mods: Joi.array()
    .items(modIDSchema)
    .min(1)
    .required()
});

const pageSchema = Joi.number()
  .integer()
  .min(0)
  .default(0);

const amountSchema = Joi.number()
  .integer()
  .min(1)
  .default(16);

(async () => {
  const app = express();

  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(bodyParser.json());
  app.use(cors());

  const dbIp = process.env.DB_IP || 'localhost';
  const dbPort = Number.parseInt(process.env.DB_PORT, 10) || 27017;
  const dbUrl = `mongodb://${dbIp}:${dbPort}`;
  const dbName = process.env.DB_NAME || 'updates';

  const port = Number.parseInt(process.env.PORT, 10) || 8088;

  const masterKey = apiKeySchema.validate(process.env.MASTER_KEY).value;

  const loginUsername = process.env.LOGIN_USERNAME;
  const loginPassword = process.env.LOGIN_PASSWORD;

  const client = await MongoClient.connect(dbUrl, {
    useNewUrlParser: true,
    useUnifiedTopology: true
  });

  const db = client.db(dbName);

  if (!(await db.listCollections({ name: 'mods' }).hasNext())) {
    db.createCollection('mods');
  }
  if (!(await db.listCollections({ name: 'updates' }).hasNext())) {
    db.createCollection('updates');
  }
  if (!(await db.listCollections({ name: 'apiKeys' }).hasNext())) {
    db.createCollection('apiKeys');
  }

  app.get('/', async (req, res, next) => {
    if (await authenticate(req)) {
      next();
    } else {
      res.set('WWW-Authenticate', 'Basic realm="401"');
      res.status(401).send('Authentication required.');
    }
  });

  app.use('/', express.static(path.join(__dirname, 'frontend/dist')));

  // Get a specific mod
  app.get('/mods/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const modCursor = await db.collection('mods').aggregate([
      {
        $match: {
          $expr: {
            $eq: ['$modID', modIDElement.value]
          }
        }
      },
      {
        $lookup: {
          from: 'updates',
          let: { id: '$_id' },
          pipeline: [
            {
              $match: {
                $expr: {
                  $eq: ['$mod', '$$id']
                }
              }
            },
            {
              $count: 'count'
            }
          ],
          as: 'updates'
        }
      },
      {
        $unwind: {
          path: '$updates',
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $addFields: {
          updateCount: {
            $ifNull: [
              {
                $arrayElemAt: [
                  {
                    $map: {
                      input: { $objectToArray: '$updates' },
                      as: 'c',
                      in: '$$c.v'
                    }
                  },
                  0
                ]
              },
              0
            ]
          }
        }
      },
      {
        $unset: 'updates'
      }
    ]);

    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    const mod = await modCursor.next();

    res.status(200).send(mod);
  });

  // Get all mods
  app.get('/mods', async (req, res) => {
    res.status(200).send(
      await db
        .collection('mods')
        .aggregate([
          {
            $lookup: {
              from: 'updates',
              let: { id: '$_id' },
              pipeline: [
                {
                  $match: {
                    $expr: {
                      $eq: ['$mod', '$$id']
                    }
                  }
                },
                {
                  $count: 'count'
                }
              ],
              as: 'updates'
            }
          },
          {
            $unwind: {
              path: '$updates',
              preserveNullAndEmptyArrays: true
            }
          },
          {
            $addFields: {
              updateCount: {
                $ifNull: [
                  {
                    $arrayElemAt: [
                      {
                        $map: {
                          input: { $objectToArray: '$updates' },
                          as: 'c',
                          in: '$$c.v'
                        }
                      },
                      0
                    ]
                  },
                  0
                ]
              }
            }
          },
          {
            $unset: 'updates'
          }
        ])
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
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    const mod = await modCursor.next();

    const updateCursor = db.collection('updates').find({ _id: new ObjectId(updateIDElement.value), mod: mod._id });
    if (!(await updateCursor.hasNext())) {
      res.status(400).send({ err: [{ message: 'Update does not exist' }] });
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

    const amountElement = amountSchema.validate(req.query.amount);
    if (amountElement.error) {
      res.status(400).send({ err: amountElement.error.details });
      res.end();
      return;
    }

    const pageElement = pageSchema.validate(req.query.page);
    if (pageElement.error) {
      res.status(400).send({ err: pageElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    const mod = await modCursor.next();

    const updates = await db
      .collection('updates')
      .find({ mod: mod._id })
      .sort({ publishDate: -1, gameVersion: -1 })
      .skip(amountElement.value * pageElement.value)
      .limit(amountElement.value)
      .toArray();
    res.status(200).send(updates);
  });

  // Get all updates
  app.get('/updates', async (req, res) => {
    const amountElement = amountSchema.validate(req.query.amount);
    if (amountElement.error) {
      res.status(400).send({ err: amountElement.error.details });
      res.end();
      return;
    }

    const pageElement = pageSchema.validate(req.query.page);
    if (pageElement.error) {
      res.status(400).send({ err: pageElement.error.details });
      res.end();
      return;
    }

    const updates = await db
      .collection('updates')
      .aggregate([
        {
          $sort: {
            publishDate: -1, gameVersion: -1
          }
        },
        {
          $skip: amountElement.value * pageElement.value
        },
        {
          $limit: amountElement.value
        },
        {
          $lookup: {
            from: 'mods',
            localField: 'mod',
            foreignField: '_id',
            as: 'mod'
          }
        },
        {
          $unwind: '$mod'
        }
      ])
      .toArray();
    res.status(200).send(updates);
  });

  // Add an update
  app.post('/updates/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    if (!(await checkAuthRequest(req, modIDElement.value))) {
      res.status(401).end();
      return;
    }

    const bodyElement = updateSchema.validate(req.body);
    if (bodyElement.error) {
      res.status(400).send({ err: bodyElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    const modExists = await modCursor.hasNext();
    if (!modExists) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }
    const result = await db.collection('updates').insertOne({
      ...bodyElement.value,
      mod: (await modCursor.next())._id
    });
    if (result.insertedCount !== 1) {
      res.status(400).send({ err: [{ message: 'Unknown Error' }] });
      return;
    }
    res.status(200).end();
  });

  // Edit an update
  app.post('/updates/:modID/:updateID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    if (!(await checkAuthRequest(req, modIDElement.value))) {
      res.status(401).end();
      return;
    }

    const updateIDElement = objectIDSchema.validate(req.params.updateID);
    if (updateIDElement.error) {
      res.status(400).send({ err: updateIDElement.error.details });
      res.end();
      return;
    }

    const bodyElement = updateSchema.validate(req.body);
    if (bodyElement.error) {
      res.status(400).send({ err: bodyElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    const modExists = await modCursor.hasNext();
    if (!modExists) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    const result = await db.collection('updates').updateMany({ _id: new ObjectId(updateIDElement.value) }, { $set: bodyElement.value });
    if (result.result.n <= 0) {
      res.status(400).send({ err: [{ message: 'Update does not exist' }] });
      return;
    }

    res.status(200).end();
  });

  // Delete an update
  app.delete('/updates/:modID/:updateID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    if (!(await checkAuthRequest(req, modIDElement.value))) {
      res.status(401).end();
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
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    const mod = await modCursor.next();

    const result = await db.collection('updates').deleteMany({ _id: new ObjectId(updateIDElement.value), mod: mod._id });
    if (result.deletedCount <= 0) {
      res.status(400).send({ err: [{ message: 'Update does not exist' }] });
      return;
    }

    res.status(200).end();
  });

  // Delete a mod
  app.delete('/mods/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    if (!(await checkAuthRequest(req, modIDElement.value))) {
      res.status(401).end();
      return;
    }

    const result = await db.collection('mods').findOne({ modID: modIDElement.value });
    if (!result) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    const deleted = await db.collection('mods').deleteOne({ _id: result._id });
    if (deleted.deletedCount <= 0) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    await db.collection('updates').deleteMany({ mod: result._id });

    res.status(200).end();
  });

  // Add a mod
  app.post('/mods/add', async (req, res) => {
    if (!(await checkAuthRequest(req))) {
      res.status(401).end();
      return;
    }

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
      res.status(400).send({ err: [{ message: 'Mod already exists' }] });
      return;
    }
    const result = await db.collection('mods').insertOne(element.value);
    if (result.insertedCount !== 1) {
      res.status(400).send({ err: [{ message: 'Unknown Error' }] });
      return;
    }
    res.status(200).end();
  });

  // Edit a mod
  app.post('/mods/edit/:modID', async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    if (!(await checkAuthRequest(req, modIDElement.value))) {
      res.status(401).end();
      return;
    }

    const element = modUpdateSchema.validate(req.body);
    if (element.error) {
      res.status(400).send({ err: element.error.details });
      res.end();
      return;
    }

    const result = await db.collection('mods').updateMany({ modID: modIDElement.value }, { $set: element.value });
    if (result.result.n <= 0) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }
    res.status(200).end();
  });

  // Get the forge update format
  app.get('/forge/:modID', cache('1 minute'), async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
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

  // Get the update format for the website
  app.get('/latest/:modID', cache('1 minute'), async (req, res) => {
    const modIDElement = modIDSchema.validate(req.params.modID);
    if (modIDElement.error) {
      res.status(400).send({ err: modIDElement.error.details });
      res.end();
      return;
    }

    const modCursor = db.collection('mods').find({ modID: modIDElement.value });
    if (!(await modCursor.hasNext())) {
      res.status(400).send({ err: [{ message: 'Mod does not exist' }] });
      return;
    }

    const mod = await modCursor.next();

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

    const data = {};

    updates.reverse().forEach(update => {
      data[update.gameVersion] = update;
    });

    res.status(200).send(Object.values(data));
  });

  // Get all apiKeys
  app.get('/apikeys', async (req, res) => {
    if (!(await checkAuthMaster(req))) {
      res.status(401).end();
      return;
    }

    res.status(200).send(
      await db
        .collection('apiKeys')
        .find({})
        .toArray()
    );
  });

  // Add a new apiKey
  app.post('/apikeys/add', async (req, res) => {
    if (!(await checkAuthMaster(req))) {
      res.status(401).end();
      return;
    }

    const apiKeyElement = apiKeyModsSchema.validate(req.body);
    if (apiKeyElement.error) {
      res.status(400).send({ err: apiKeyElement.error.details });
      res.end();
      return;
    }

    const apiKey = uuid();
    const result = await db.collection('apiKeys').insertOne({ ...apiKeyElement.value, apiKey });
    if (result.insertedCount !== 1) {
      res.status(400).send({ err: 'Unknown Error' });
      return;
    }
    res.status(200).send({ apiKey });
  });

  // Delete an apiKey
  app.delete('/apikeys/:apiKey', async (req, res) => {
    if (!(await checkAuthMaster(req))) {
      res.status(401).end();
      return;
    }

    const apiKeyElement = apiKeySchema.validate(req.params.apiKey);
    if (apiKeyElement.error) {
      res.status(400).send({ err: apiKeyElement.error.details });
      res.end();
      return;
    }

    const result = await db.collection('apiKeys').deleteMany({ apiKey: apiKeyElement.value });
    if (result.result.n <= 0 || result.result.ok <= 0) {
      res.status(400).send({ err: 'ApiKey not found' });
      return;
    }
    res.status(200).end();
  });

  app.listen(port, () => {
    console.log(`Listening on port ${port}.`);
  });

  async function checkAuthRequest(req, modID = '*') {
    const apiKeyElement = apiKeySchema.validate(req.headers.apikey);
    if (apiKeyElement.error) {
      return false;
    }

    return checkAuth(apiKeyElement.value, modID); // TODO
  }

  async function checkAuth(apiKey, modID = '*') {
    if (masterKey && apiKey && apiKey.toLowerCase() === masterKey) {
      return true;
    }

    const apiKeyCursor = db.collection('apiKeys').find({ apiKey });

    if (!(await apiKeyCursor.hasNext())) {
      return false;
    }
    const apiKeyDB = await apiKeyCursor.next();

    return apiKeyDB.mods.includes('*') || apiKeyDB.mods.includes(modID);
  }

  async function checkAuthMaster(req) {
    const apiKeyElement = apiKeySchema.validate(req.headers.apikey);
    if (apiKeyElement.error) {
      return false;
    }

    return masterKey && apiKeyElement.value && apiKeyElement.value.toLowerCase() === masterKey;
  }

  async function authenticate(req) {
    const credentials = auth(req);

    if (credentials) {
      if (credentials.name.toLowerCase() === 'apikey') {
        if (await checkAuth(credentials.pass)) {
          return true;
        }
      }
      if (
        loginUsername
        && loginPassword
        && credentials.name === loginUsername
        && credentials.pass === loginPassword
      ) {
        return true;
      }
    }
    return false;
  }
})();
