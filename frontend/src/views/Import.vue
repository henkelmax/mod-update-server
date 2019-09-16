<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>Import Mod</span>
        <v-spacer></v-spacer>
        <v-btn text color="gray" @click="back()">Back</v-btn>
      </v-card-title>
      <v-card-text>
        <v-form v-model="valid" ref="form">
          <v-file-input chips :rules="rules" label="Update JSON" v-model="file" required></v-file-input>
          <v-select
            v-model="mod"
            :items="mods"
            :rules="stringRequiredRules"
            item-text="name"
            item-value="modID"
            label="Mod"
            required
          ></v-select>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn class="mr-6 mb-2" @click="importMod" :disabled="!valid">Import</v-btn>
      </v-card-actions>
    </v-card>
    <v-snackbar v-model="snackbar" bottom color="error" multi-line :timeout="6000">
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
      mods: [],
      mod: null,
      snackbar: false,
      error: "",
      rules: [
        value => !!value || "This field is required",
        value =>
          !value ||
          !value.name ||
          value.name.toLowerCase().endsWith(".json") ||
          "The file has to be a .json file",
        value =>
          !value ||
          value.size < 2000000 ||
          "The file should be smaller than 2 MB"
      ],
      stringRequiredRules: [v => !!v || "This field is required"]
    };
  },
  created() {
    axios
      .get(`${this.server}/mods`)
      .then(response => {
        this.mods = response.data;
      })
      .catch(err => {
        this.error = "Could not retrieve mods";
        this.snackbar = true;
      });
  },
  methods: {
    back() {
      this.$router.push({
        path: "mods"
      });
    },
    importMod() {
      if (!this.$refs.form.validate()) {
        this.snackbar = true;
        this.error = "Please check your fields";
        return;
      }
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const json = JSON.parse(reader.result);
          const updates = this.toUpdates(json);
          const promises = [];
          for (let update of updates) {
            promises.push(
              axios.post(`${this.server}/updates/${this.mod}`, update, {
                headers: {
                  apikey: sessionStorage.apiKey
                }
              })
            );
          }
          Promise.all(promises)
            .then(response => {
              this.back();
            })
            .catch(err => {
              this.error = "Could not add updates";
              this.snackbar = true;
            });
        } catch (err) {
          this.error = "Failed to parse JSON";
          this.snackbar = true;
        }
      };
      reader.readAsText(this.file);
    },
    toUpdates(json) {
      const updates = [];

      const latestUpdates = {};
      const recommendedUpdates = {};
      for (let mcVersion in json) {
        const latest = json.promos[`${mcVersion}-latest`];
        const recommended = json.promos[`${mcVersion}-recommended`];

        if (latest) {
          latestUpdates[mcVersion] = latest;
        }
        if (recommended) {
          recommendedUpdates[mcVersion] = recommended;
        }
      }

      for (let mcVersion in json) {
        if (mcVersion === "homepage" || mcVersion === "promos") {
          continue;
        }
        let count = 10;
        for (let modVersion of Object.keys(json[mcVersion]).reverse()) {
          const update = {
            publishDate: moment()
              .subtract(count, "seconds")
              .format(),
            gameVersion: mcVersion,
            version: modVersion,
            updateMessages: [json[mcVersion][modVersion]],
            releaseType: "release",
            tags: []
          };

          if (
            recommendedUpdates[mcVersion] &&
            recommendedUpdates[mcVersion] === modVersion
          ) {
            update.tags.push("recommended");
            update.publishDate = moment().format();
          }

          if (
            latestUpdates[mcVersion] &&
            latestUpdates[mcVersion] === modVersion
          ) {
            update.publishDate = moment().format();
          }

          updates.push(update);
          count++;
        }
      }

      return updates;
    }
  }
};
</script>
