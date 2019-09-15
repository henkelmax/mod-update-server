<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>API Keys</span>
        <v-spacer></v-spacer>
        <v-btn text color="primary" @click="addDialog = true">Add API Key</v-btn>
        <v-btn text color="gray" @click="back()">Back</v-btn>
      </v-card-title>
      <v-list>
        <template v-for="(apiKey, i) in apiKeys">
          <div :key="apiKey.apiKey">
            <v-list-item>
              <v-list-item-content>
                <v-list-item-title v-text="apiKey.apiKey"></v-list-item-title>
                <v-list-item-subtitle v-text="apiKey.mods.join(', ')"></v-list-item-subtitle>
              </v-list-item-content>
              <v-list-item-action>
                <v-btn text icon color="gray" @click="copy(apiKey.apiKey)">
                  <v-icon>mdi-clipboard</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action>
                <v-btn text icon color="gray" @click="openDeleteDialog(apiKey)">
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
            <v-divider v-if="i!==apiKeys.length-1" class="ml-2 mr-2"></v-divider>
          </div>
        </template>
      </v-list>
    </v-card>
    <v-dialog v-model="deleteDialog" max-width="35%">
      <v-card>
        <v-card-title class="headline">Delete {{ apiKeyToDelete.apiKey }}</v-card-title>
        <v-card-text>Do you really want to delete this API key?</v-card-text>
        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="darken-1" text @click="dialog = false">No</v-btn>
          <v-btn color="primary darken-1" text @click="removeApiKey">Yes</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="addDialog" max-width="35%">
      <v-card>
        <v-card-title class="headline">Add a new API Key</v-card-title>
        <v-card-text>
          Which mods should be allowed? (* for all)
          <br />
          <v-text-field v-model="apiKeyToAdd" label="Mods (Comma separated)"></v-text-field>
        </v-card-text>

        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="primary darken-1" text @click="addApiKey">OK</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
      apiKeys: [],
      deleteDialog: false,
      addDialog: false,
      apiKeyToDelete: {},
      apiKeyToAdd: "",
      snackbar: false,
      error: ""
    };
  },
  created() {
    this.updateApiKeys();
  },
  methods: {
    updateApiKeys() {
      axios
        .get(`${this.server}/apikeys`, {
          headers: {
            apikey: sessionStorage.apiKey
          }
        })
        .then(response => {
          this.apiKeys = response.data;
        })
        .catch(err => {
          if (err.response && err.response.status === 401) {
            this.error = "You have no permission to view API Keys";
          } else {
            this.error = "Could not retrieve API Keys";
          }
          this.snackbar = true;
        });
    },
    removeApiKey() {
      this.deleteDialog = false;
      axios
        .delete(`${this.server}/apikeys/${this.apiKeyToDelete.apiKey}`, {
          headers: {
            apikey: sessionStorage.apiKey
          }
        })
        .then(response => {
          this.updateApiKeys();
        })
        .catch(err => {
          this.error = "Could not delete API Key";
          this.snackbar = true;
        });
    },
    openDeleteDialog(apiKey) {
      this.deleteDialog = true;
      this.apiKeyToDelete = apiKey;
    },
    addApiKey() {
      axios
        .post(
          `${this.server}/apikeys/add`,
          { mods: this.apiKeyToAdd.split(",") },
          {
            headers: {
              apikey: sessionStorage.apiKey
            }
          }
        )
        .then(response => {
          this.apiKeyToAdd = "";
          this.addDialog = false;
          this.updateApiKeys();
        })
        .catch(err => {
          this.apiKeyToAdd = "";
          this.addDialog = false;
          this.error = "Could not add API Key";
          this.snackbar = true;
        });
    },
    copy(text) {
      this.$clipboard(text);
    },
    back() {
      this.$router.push({
        path: "mods"
      });
    }
  }
};
</script>