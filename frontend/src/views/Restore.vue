<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>Restore Backup</span>
        <v-spacer></v-spacer>
        <v-btn text color="gray" @click="back()">Back</v-btn>
      </v-card-title>
      <v-card-text>
        <v-form v-model="valid" ref="form">
          <v-file-input
            chips
            :rules="rules"
            label="Backup JSON"
            v-model="file"
            required
          ></v-file-input>
        </v-form>
        <v-row justify="center">
          <v-progress-circular
            v-if="processing"
            :size="70"
            :width="7"
            color="blue"
            indeterminate
          ></v-progress-circular>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn class="mr-6 mb-2" @click="restore" :disabled="!valid"
          >Restore</v-btn
        >
      </v-card-actions>
    </v-card>
    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
      multi-line
      :timeout="6000"
    >
      {{ error }}
      <v-btn dark text @click="snackbar = false">Close</v-btn>
    </v-snackbar>
  </v-container>
</template>

<script>
import axios from "axios";
import moment from "moment";

export default {
  components: {},
  data() {
    return {
      valid: false,
      file: null,
      snackbar: false,
      processing: false,
      error: "",
      rules: [
        (value) => !!value || "This field is required",
        (value) =>
          !value ||
          !value.name ||
          value.name.toLowerCase().endsWith(".json") ||
          "The file has to be a .json file",
      ]
    };
  },
  methods: {
    back() {
      this.$router.push({
        path: "mods"
      });
    },
    restore() {
      if (!this.$refs.form.validate()) {
        this.snackbar = true;
        this.error = "Please check your fields";
        return;
      }
      const reader = new FileReader();
      reader.onload = async () => {
        this.processing = true;
        try {
          const json = JSON.parse(reader.result);
          if (!json.mods) {
            throw new Error("Invalid JSON");
          }

          const mods = (await axios.get(`${this.server}/mods`)).data;

          if (!mods) {
            throw new Error("Could not retrieve mods");
          }

          if (mods.length > 0) {
            throw new Error("Data is not empty");
          }

          const promises = [];

          for (const mod of json.mods) {
            const promise = this.restoreMod(mod);
            promises.push(promise);
            promise
              .then(() => {})
              .catch((err) => {
                this.error = err;
                this.snackbar = true;
              });
          }
          Promise.all(promises).then(() => {
            this.back();
          });
        } catch (err) {
          this.error = err;
          this.snackbar = true;
        }
        this.processing = false;
      };
      reader.readAsText(this.file);
    },
    async restoreMod(mod) {
      const response = await axios.post(
        `${this.server}/mods/add`,
        {
          modID: mod.modID,
          name: mod.name,
          description: mod.description,
          websiteURL: mod.websiteURL,
          downloadURL: mod.downloadURL,
          issueURL: mod.issueURL
        },
        {
          headers: {
            apikey: sessionStorage.apiKey
          },
        }
      );

      if (response.status < 200 || response.status >= 300) {
        throw new Error(`Could not add mod '${mod.name}'`);
      }

      for (const update of mod.updates) {
        await axios.post(
          `${this.server}/updates/${mod.modID}`,
          {
            publishDate: update.publishDate,
            gameVersion: update.gameVersion,
            modLoader: update.modLoader,
            version: update.version,
            updateMessages: update.updateMessages,
            releaseType: update.releaseType,
            tags: update.tags
          },
          {
            headers: {
              apikey: sessionStorage.apiKey
            }
          }
        );
      }
    }
  }
};
</script>
