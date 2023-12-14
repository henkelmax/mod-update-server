<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-toolbar color="secondary">
        <v-toolbar-title>Backup Mods and Updates</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn text color="white" @click="back">Back</v-btn>
      </v-toolbar>
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
        <v-btn class="mr-6 mb-2" @click="runBackup">Backup</v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref } from "vue";
import moment from "moment";
import download from "js-file-download";
import router from "@/router";
import { backup, showHttpErrorMessage } from "@/services";

const processing = ref(false);

function back() {
  router.push("mods");
}

async function runBackup() {
  processing.value = true;
  try {
    const backupJson = await backup();
    download(
      JSON.stringify(backupJson, null, 2),
      `backup_${moment()
        .format("YYYY-MM-DD-HH:mm")}.json`
    );
  } catch (err) {
    showHttpErrorMessage(err);
  }
  processing.value = false;
}
</script>
