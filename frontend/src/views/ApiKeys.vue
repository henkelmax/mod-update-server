<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-toolbar color="secondary">
        <v-toolbar-title>API Keys</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn text color="white" @click="addDialog = true">Add API Key</v-btn>
        <v-btn text color="white" @click="back">Back</v-btn>
      </v-toolbar>
      <v-list>
        <template v-for="(apiKey, i) in apiKeys" :key="apiKey.apiKey">
          <v-list-item :title="apiKey.apiKey" :subtitle="apiKey.mods.join(', ')">
            <template v-slot:append>
              <v-btn
                color="grey-darken-2"
                icon="mdi-clipboard"
                variant="text"
                @click.stop="copy(apiKey.apiKey)"
              ></v-btn>
              <v-btn
                color="grey-darken-2"
                icon="mdi-delete"
                variant="text"
                @click.stop="openDeleteDialog(apiKey)"
              ></v-btn>
            </template>
          </v-list-item>
          <v-divider v-if="i !== apiKeys.length - 1" class="ml-2 mr-2"></v-divider>
        </template>
      </v-list>
    </v-card>
    <v-dialog v-model="deleteDialog" max-width="35%">
      <v-card>
        <v-card-title class="headline">Delete {{ apiKeyToDelete.apiKey }}</v-card-title>
        <v-card-text>Do you really want to delete this API key?</v-card-text>
        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="red" text @click="deleteDialog = false">No</v-btn>
          <v-btn color="green" text @click="removeApiKey">Yes</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="addDialog" max-width="35%">
      <v-card>
        <v-card-title class="headline">Add a new API Key</v-card-title>
        <v-card-text>
          Which mods should be allowed? (* for all)
          <br />
          <v-text-field
            v-model="apiKeyToAdd"
            label="Mods (Comma separated)"
          ></v-text-field>
        </v-card-text>

        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="green" text @click="generateApiKey">OK</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getApiKeys, deleteApiKey, addApiKey, showHttpErrorMessage, showErrorMessage } from "@/services";
import router from "@/router";

const apiKeys = ref([]);
const deleteDialog = ref(false);
const addDialog = ref(false);
const apiKeyToDelete = ref({});
const apiKeyToAdd = ref("");

onMounted(async () => {
  updateApiKeys();
});

function updateApiKeys() {
  getApiKeys()
    .then((response) => {
      apiKeys.value = response;
    })
    .catch((err) => {
      if (err.response?.status === 401) {
        showErrorMessage("You have no permission to view API Keys");
      } else {
        showHttpErrorMessage(err);
      }
    });
}

function removeApiKey() {
  deleteDialog.value = false;
  deleteApiKey(apiKeyToDelete.value.apiKey)
    .then(() => {
      updateApiKeys();
    })
    .catch(showHttpErrorMessage);
}

function openDeleteDialog(apiKey) {
  deleteDialog.value = true;
  apiKeyToDelete.value = apiKey;
}

function generateApiKey() {
  addApiKey(apiKeyToAdd.value.split(","))
    .then(() => {
      apiKeyToAdd.value = "";
      addDialog.value = false;
      updateApiKeys();
    })
    .catch((err) => {
      apiKeyToAdd.value = "";
      addDialog.value = false;
      showHttpErrorMessage(err);
    });
}
function copy(text) {
  navigator.clipboard.writeText(text);
}
function back() {
  router.push("mods");
}
</script>
