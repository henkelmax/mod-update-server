<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>{{ newMod ? 'Add a new mod' : `Edit ${mod.modID}` }}</span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-container>
            <v-row>
              <v-col v-if="newMod" cols="12" md="12">
                <v-text-field v-model="mod.modID" :rules="modIDRules" label="Mod ID" required></v-text-field>
              </v-col>

              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.name"
                  :rules="stringRequiredRules"
                  label="Mod name"
                  required
                ></v-text-field>
              </v-col>

              <v-col cols="12" md="12">
                <v-textarea
                  v-model="mod.description"
                  :rules="stringRequiredRules"
                  label="Description"
                  required
                ></v-textarea>
              </v-col>

              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.websiteURL"
                  :rules="stringRequiredRules"
                  label="Website URL"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.downloadURL"
                  :rules="stringRequiredRules"
                  label="Download URL"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.issueURL"
                  :rules="stringRequiredRules"
                  label="Issue Tracker URL"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn class="mr-4 mb-2" @click="back">Back</v-btn>
        <v-btn
          :disabled="!valid"
          color="success"
          class="mr-6 mb-2"
          @click="validate"
        >{{ newMod ? 'Add' : 'Update' }}</v-btn>
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

export default {
  components: {},
  data() {
    return {
      newMod: true,
      mod: {},
      valid: false,
      error: "",
      snackbar: false,
      modIDRules: [
        v => !!v || "This field is required",
        v => /^[a-z-_]*$/.test(v) || "The Mod ID can only contain [a-z]"
      ],
      stringRequiredRules: [v => !!v || "This field is required"]
    };
  },
  created() {
    if (!this.$route.query.modID) {
      return;
    }
    this.newMod = false;

    axios
      .get(`${this.server}/mods/${this.$route.query.modID}`)
      .then(response => {
        this.mod = response.data;
      });
  },
  methods: {
    validate() {
      if (!this.$refs.form.validate()) {
        this.snackbar = true;
        this.error = "Please check your fields";
        return;
      }

      if (this.newMod) {
        axios
          .post(`${this.server}/mods/add`, this.mod, {
            headers: {
              apikey: sessionStorage.apiKey
            }
          })
          .then(response => {
            this.$router.push({ path: "mods" });
          })
          .catch(err => {
            this.showError(err);
          });
      } else {
        const m = Object.assign({}, this.mod);
        delete m.modID;
        delete m._id;
        axios
          .post(`${this.server}/mods/edit/${this.mod.modID}`, m, {
            headers: {
              apikey: sessionStorage.apiKey
            }
          })
          .then(response => {
            this.$router.push({ path: "mods" });
          })
          .catch(err => {
            this.showError(err);
          });
      }
    },
    showError(err) {
      this.snackbar = true;
      if (err.response) {
        this.error = `${err.response.statusText}: ${
          err.response.data.err ? err.response.data.err : ""
        }`;
      } else {
        this.error = "Unknown Error";
      }
    },
    back() {
      this.$router.push({ path: "mods" });
    }
  }
};
</script>
