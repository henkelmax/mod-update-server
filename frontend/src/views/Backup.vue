<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>Backup Mods and Updates</span>
        <v-spacer></v-spacer>
        <v-btn text color="gray" @click="back()">Back</v-btn>
      </v-card-title>
      <v-card-text>
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
        <v-btn class="mr-6 mb-2" @click="backup">Backup</v-btn>
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
import download from "js-file-download";

export default {
  components: {},
  data() {
    return {
      snackbar: false,
      processing: false,
      error: ""
    };
  },
  methods: {
    back() {
      this.$router.push({
        path: "mods"
      });
    },
    async backup() {
      this.processing = true;
      try {
        const backup = { backupDate: moment().format(), mods: [] };

        const promises = [];

        const mods = (await axios.get(`${this.server}/mods`)).data;
        for (const mod of mods) {
          const promise = axios.get(
            `${this.server}/updates/${mod.modID}?amount=${Number.MAX_SAFE_INTEGER}`
          );
          promises.push(promise);

          promise.then((response) => {
            mod.updates = response.data;
            backup.mods.push(mod);
          });
        }
        const x = await Promise.all(promises);
        download(
          JSON.stringify(backup, null, 2),
          `backup_${moment().format("YYYY-MM-DD-HH:mm")}.json`
        );
      } catch (err) {
        this.error = "Could not backup data";
        this.snackbar = true;
      }
      this.processing = false;
    }
  }
};
</script>
