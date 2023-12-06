<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-toolbar color="secondary">
        <v-toolbar-title>Restore Backup</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn text color="white" @click="back">Back</v-btn>
      </v-toolbar>
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
        <v-btn class="mr-6 mb-2" color="green" @click="restoreBackup" :disabled="!valid">
          Restore</v-btn
        >
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
import { ref } from "vue";
import router from "@/router";
import { getMods, restore, getErrorMessage } from "@/services";

const valid = ref(false);
const file = ref(null);
const snackbar = ref(false);
const processing = ref(false);
const error = ref("");

const rules = [
  (value) => !!value || "This field is required",
  (value) =>
    !value ||
    !value.name ||
    value.name.toLowerCase().endsWith(".json") ||
    "The file has to be a .json file",
];

function back() {
  router.push("mods");
}

function restoreBackup() {
  if (!valid.value) {
    snackbar.value = true;
    error.value = "Please check your fields";
    return;
  }
  const reader = new FileReader();
  reader.onload = async () => {
    processing.value = true;
    try {
      const json = JSON.parse(reader.result);
      if (!json.mods) {
        showError("Invalid JSON");
        return;
      }

      const mods = await getMods();

      if (mods.length > 0) {
        showError("Please remove all data before restoring a backup");
        return;
      }

      await restore(json);

      back();
    } catch (err) {
      showError(getErrorMessage(err));
    }
    processing.value = false;
  };

  if (file.value && file.value.length > 0) {
    reader.readAsText(file.value[0]);
  } else {
    showError("Please only upload one file");
  }
}

function showError(message) {
  error.value = message;
  snackbar.value = true;
  processing.value = false;
}
</script>
