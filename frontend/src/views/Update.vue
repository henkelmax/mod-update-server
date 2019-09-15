<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>{{ newUpdate ? 'Add a new update' : `Edit ${mod.name} ${update.version}` }}</span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-container>
            <v-row>
              <v-col cols="12" md="6">
                <v-menu
                  ref="timeMenu"
                  v-model="timeMenu"
                  :close-on-content-click="false"
                  :nudge-right="40"
                  :return-value.sync="publishTime"
                  transition="scale-transition"
                  offset-y
                  max-width="290px"
                  min-width="290px"
                >
                  <template v-slot:activator="{ on }">
                    <v-text-field v-model="publishTime" label="Publish Time" readonly v-on="on"></v-text-field>
                  </template>
                  <v-time-picker
                    v-if="timeMenu"
                    v-model="publishTime"
                    format="24hr"
                    @click:minute="$refs.timeMenu.save(publishTime)"
                  ></v-time-picker>
                </v-menu>
              </v-col>
              <v-col cols="12" md="6">
                <v-menu
                  v-model="dateMenu"
                  :close-on-content-click="false"
                  :nudge-right="40"
                  transition="scale-transition"
                  offset-y
                  min-width="290px"
                >
                  <template v-slot:activator="{ on }">
                    <v-text-field v-model="publishDate" label="Publish Date" readonly v-on="on"></v-text-field>
                  </template>
                  <v-date-picker v-model="publishDate" @input="dateMenu = false"></v-date-picker>
                </v-menu>
              </v-col>

              <v-col cols="12" md="12">
                <v-text-field
                  v-model="update.gameVersion"
                  :rules="stringRequiredRules"
                  label="Game version"
                  required
                ></v-text-field>
              </v-col>

              <v-col cols="12" md="12">
                <v-text-field
                  v-model="update.version"
                  :rules="stringRequiredRules"
                  label="Mod version"
                  required
                ></v-text-field>
              </v-col>

              <v-col cols="12" md="12">
                <v-textarea
                  v-model="updateMessages"
                  :rules="stringRequiredRules"
                  label="Update Message"
                  required
                ></v-textarea>
              </v-col>

              <v-col cols="12" md="12">
                <v-radio-group row v-model="update.releaseType" :mandatory="true">
                  <v-radio label="Alpha" value="alpha"></v-radio>
                  <v-radio label="Beta" value="beta"></v-radio>
                  <v-radio label="Release" value="release"></v-radio>
                </v-radio-group>
              </v-col>
              <v-col cols="12" md="12">
                <v-checkbox v-model="update.tags" value="recommended" label="Recommended"></v-checkbox>
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
        >{{ newUpdate ? 'Add' : 'Update' }}</v-btn>
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
      newUpdate: true,
      update: { tags: [], publishDate: moment().format() },
      mod: {},
      valid: false,
      error: "",
      snackbar: false,
      timeMenu: false,
      dateMenu: false,
      publishTime: moment().format("HH:mm"),
      publishDate: moment().format("YYYY-MM-DD"),
      stringRequiredRules: [v => !!v || "This field is required"]
    };
  },
  created() {
    if (!this.$route.query.modID || !this.$route.query.updateID) {
      return;
    }
    this.newUpdate = false;

    axios
      .get(`${this.server}/mods/${this.$route.query.modID}`)
      .then(response => {
        this.mod = response.data;
      });

    axios
      .get(
        `${this.server}/updates/${this.$route.query.modID}/${this.$route.query.updateID}`
      )
      .then(response => {
        this.update = response.data;
        delete this.update._id;
        delete this.update.mod;
        this.publishTime = moment(this.update.publishDate).format("HH:mm");
        this.publishDate = moment(this.update.publishDate).format("YYYY-MM-DD");
      });
  },
  computed: {
    updateMessages: {
      get() {
        if (!this.update.updateMessages) {
          return "";
        }
        return this.update.updateMessages.join("\n");
      },
      set(value) {
        this.update.updateMessages = value.split("\n");
      }
    }
  },
  watch: {
    publishTime(time) {
      const val = moment(time, "HH:mm");
      this.update.publishDate = moment(this.update.publishDate)
        .set({
          hour: val.hour(),
          minute: val.minute(),
          second: 0,
          millisecond: 0
        })
        .format();
    },
    publishDate(date) {
      const val = moment(date, "YYYY-MM-DD");
      this.update.publishDate = moment(this.update.publishDate)
        .set({
          date: val.date(),
          month: val.month(),
          year: val.year()
        })
        .format();
    }
  },
  methods: {
    validate() {
      if (!this.$refs.form.validate()) {
        this.snackbar = true;
        this.error = "Please check your fields";
        return;
      }

      if (!this.$route.query.modID) {
        return;
      }

      if (this.newUpdate) {
        axios
          .post(
            `${this.server}/updates/${this.$route.query.modID}`,
            this.update,
            {
              headers: {
                apikey: sessionStorage.apiKey
              }
            }
          )
          .then(response => {
            this.$router.push({
              path: "updates",
              query: { modID: this.$route.query.modID }
            });
          })
          .catch(err => {
            this.showError(err);
          });
      } else {
        axios
          .post(
            `${this.server}/updates/${this.$route.query.modID}/${this.$route.query.updateID}`,
            this.update,
            {
              headers: {
                apikey: sessionStorage.apiKey
              }
            }
          )
          .then(response => {
            this.$router.push({
              path: "updates",
              query: { modID: this.$route.query.modID }
            });
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
      this.$router.go(-1);
    }
  }
};
</script>
