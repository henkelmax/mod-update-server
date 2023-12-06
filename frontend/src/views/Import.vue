<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-toolbar color="secondary">
        <v-toolbar-title>Import Mod</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn text color="white" @click="back">Back</v-btn>
      </v-toolbar>
      <v-card-text>
        <v-form v-model="valid" ref="form">
          <v-file-input
            chips
            :rules="rules"
            label="Update JSON"
            v-model="file"
            required
          ></v-file-input>
          <v-select
            v-model="mod"
            :items="mods"
            :rules="stringRequiredRules"
            item-title="name"
            item-value="modID"
            label="Mod"
            required
          ></v-select>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn class="mr-6 mb-2" color="green" @click="importMod" :disabled="!valid">
          Import
        </v-btn>
      </v-card-actions>
    </v-card>
    <v-snackbar v-model="snackbar" bottom color="error" multi-line :timeout="6000">
      {{ error }}
      <template v-slot:actions>
        <v-btn dark text @click="snackbar = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getMods, addUpdate, getErrorMessage } from "@/services";
import router from "@/router";
import moment from "moment";

const valid = ref(false);
const file = ref(null);
const mods = ref([]);
const mod = ref(null);
const snackbar = ref(false);
const error = ref("");

const rules = [
  (value) => !!value || "This field is required",
  (value) =>
    !value ||
    !value.name ||
    value.name.toLowerCase().endsWith(".json") ||
    "The file has to be a .json file",
  (value) => !value || value.size < 2000000 || "The file should be smaller than 2 MB",
];
const stringRequiredRules = [(v) => !!v || "This field is required"];

onMounted(async () => {
  getMods()
    .then((response) => {
      mods.value = response;
    })
    .catch((err) => {
      error.value = getErrorMessage(err);
      snackbar.value = true;
    });
});

function back() {
  router.push("mods");
}

function importMod() {
  if (!valid.value) {
    snackbar.value = true;
    error.value = "Please check your fields";
    return;
  }
  const reader = new FileReader();
  reader.onload = () => {
    try {
      const json = JSON.parse(reader.result);
      const updates = toUpdates(json);
      const promises = [];
      for (let update of updates) {
        promises.push(addUpdate(mod.value.modID, update));
      }
      Promise.all(promises)
        .then(() => {
          back();
        })
        .catch((err) => {
          error.value = getErrorMessage(err);
          snackbar.value = true;
        });
    } catch (err) {
      error.value = "Failed to parse JSON";
      snackbar.value = true;
    }
  };
  reader.readAsText(file.value);
}

function toUpdates(json) {
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
        publishDate: moment().subtract(count, "seconds").format(),
        gameVersion: mcVersion,
        version: modVersion,
        updateMessages: [json[mcVersion][modVersion]],
        releaseType: "release",
        tags: [],
      };

      if (recommendedUpdates[mcVersion] && recommendedUpdates[mcVersion] === modVersion) {
        update.tags.push("recommended");
        update.publishDate = moment().format();
      }

      if (latestUpdates[mcVersion] && latestUpdates[mcVersion] === modVersion) {
        update.publishDate = moment().format();
      }

      updates.push(update);
      count++;
    }
  }

  return updates;
}
</script>
