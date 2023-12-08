<template>
  <v-card max-width="75%" class="mx-auto mt-4">
    <v-toolbar color="secondary">
      <v-toolbar-title>Latest Updates</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn text color="white" @click="back">Back</v-btn>
    </v-toolbar>
    <template v-if="updates.length > 0">
      <v-list lines="two">
        <template v-for="(update, i) in updates" :key="update._id">
          <v-list-item
            :title="`[${update.modLoader.toUpperCase()}] [${update.gameVersion}] ${
              update.mod.name
            } ${update.version}`"
          >
            <template v-slot:subtitle>
              <div class="mb-2">
                {{ `Released ${moment(update.publishDate).fromNow()}` }}
              </div>
              <div v-for="(str, idx) in update.updateMessages" :key="idx">
                {{ str }}
              </div>
            </template>
            <template v-slot:append>
              <v-btn
                color="grey-darken-2"
                icon="mdi-pencil"
                variant="text"
                @click.stop="editUpdate(update._id)"
              ></v-btn>
              <v-btn
                color="grey-darken-2"
                icon="mdi-delete"
                variant="text"
                @click.stop="openDeleteDialog(update)"
              ></v-btn>
            </template>
          </v-list-item>
          <v-divider v-if="i !== updates.length - 1" class="ml-2 mr-2"></v-divider>
        </template>
      </v-list>
    </template>
    <template v-else>
      <v-card-text class="text-center"> No updates available </v-card-text>
    </template>
  </v-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getLatestUpdates } from "@/services";
import { showHttpErrorMessage } from "@/services/messages";
import router from "@/router";
import moment from "moment";

const updates = ref([]);

onMounted(async () => {
  getLatestUpdates(32, 0)
    .then((response) => {
      updates.value = response;
    })
    .catch(showHttpErrorMessage);
});

function back() {
  router.push("/");
}
</script>
