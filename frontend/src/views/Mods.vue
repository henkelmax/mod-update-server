<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>Mods</span>
        <v-spacer></v-spacer>
        <v-btn text color="primary" @click="addMod()">Add Mod</v-btn>
        <v-btn text color="primary" @click="importMod()">Import Updates</v-btn>
        <v-btn text color="primary" @click="backup()">Backup</v-btn>
        <v-btn text color="primary" @click="restore()">Restore</v-btn>
      </v-card-title>
      <v-list>
        <template v-for="(mod, i) in mods">
          <div :key="mod.modID">
            <v-list-item>
              <v-list-item-avatar>
                <v-btn text icon color="gray" @click="addUpdate(mod.modID)">
                  <v-icon>mdi-file-document-box-outline</v-icon>
                </v-btn>
              </v-list-item-avatar>

              <v-list-item-content>
                <v-list-item-title v-text="mod.name"></v-list-item-title>
                <v-list-item-subtitle v-text="mod.description"></v-list-item-subtitle>
              </v-list-item-content>
              <v-list-item-action>
                <v-btn text icon color="gray" @click="editMod(mod.modID)">
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action>
                <v-btn text icon color="gray" @click="openDeleteDialog(mod)">
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
            <v-divider v-if="i!==mods.length-1" class="ml-2 mr-2"></v-divider>
          </div>
        </template>
      </v-list>
    </v-card>
    <v-dialog v-model="dialog" max-width="35%">
      <v-card>
        <v-card-title class="headline">Delete {{ modToDelete.name }}</v-card-title>
        <v-card-text>Do you really want to delete this mod?</v-card-text>
        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="darken-1" text @click="dialog = false">No</v-btn>
          <v-btn color="primary darken-1" text @click="removeMod">Yes</v-btn>
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
      mods: [],
      dialog: false,
      modToDelete: {},
      snackbar: false,
      error: ""
    };
  },
  created() {
    this.updateMods();
  },
  methods: {
    updateMods() {
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
    addUpdate(modID) {
      this.$router.push({ path: "updates", query: { modID: modID } });
    },
    editMod(modID) {
      this.$router.push({ path: "mod", query: { modID: modID } });
    },
    removeMod() {
      this.dialog = false;
      axios
        .delete(`${this.server}/mods/${this.modToDelete.modID}`, {
          headers: {
            apikey: sessionStorage.apiKey
          }
        })
        .then(response => {
          this.updateMods();
        })
        .catch(err => {
          this.error = "Could not delete mod";
          this.snackbar = true;
        });
    },
    addMod() {
      this.$router.push({ path: "mod" });
    },
    importMod() {
      this.$router.push({ path: "import" });
    },
    backup() {
      this.$router.push({ path: "backup" });
    },
    restore() {
      this.$router.push({ path: "restore" });
    },
    openDeleteDialog(mod) {
      this.dialog = true;
      this.modToDelete = mod;
    }
  }
};
</script>
