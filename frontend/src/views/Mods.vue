<template>
  <v-card max-width="75%" class="mx-auto mt-4">
    <v-toolbar color="secondary">
      <v-toolbar-title>Mods</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn text color="white" @click="router.push('latest')"> Latest</v-btn>
      <v-btn text color="white" @click="router.push('mod')"> Add Mod</v-btn>
      <v-btn text color="white" @click="router.push('import')"> Import Updates</v-btn>
      <v-btn text color="white" @click="router.push('backup')"> Backup</v-btn>
      <v-btn text color="white" @click="router.push('restore')"> Restore</v-btn>
      <v-btn text color="white" @click="router.push('apikeys')"> API Keys</v-btn>
      <v-btn text color="white" @click="doLogout"> Log Out</v-btn>
    </v-toolbar>
    <v-text-field
      class="mx-4"
      :loading="false"
      v-model="searchText"
      variant="underlined"
      label="Search Mods"
      append-inner-icon="mdi-magnify"
      single-line
      hide-details
      flat
    ></v-text-field>
    <v-list lines="two">
      <template v-for="(mod, i) in filteredMods" :key="mod.modID">
        <v-list-item @click="openUpdates(mod.modID)">
          <v-list-item-title class="text-h6">
            {{ mod.name }}
          </v-list-item-title>
          <div class="mb-1 text-grey-lighten-1 text-subtitle-2">
            {{ mod.description }}
          </div>
          <template v-slot:append>
            <v-btn
              color="grey-darken-2"
              icon="mdi-pencil"
              variant="text"
              @click.stop="editMod(mod.modID)"
            ></v-btn>
            <v-btn
              color="grey-darken-2"
              icon="mdi-delete"
              variant="text"
              @click.stop="openDeleteDialog(mod)"
            ></v-btn>
          </template>
        </v-list-item>
        <v-divider v-if="i !== mods.length - 1" class="ml-2 mr-2"></v-divider>
      </template>
    </v-list>
    <v-card-text v-if="filteredMods.length <= 0" class="text-center">
      There are no mods matching your search
    </v-card-text>
  </v-card>
  <v-container>
    <v-dialog v-model="dialog" max-width="35%">
      <v-card>
        <v-card-title class="headline"> Delete {{ modToDelete.name }}</v-card-title>
        <v-card-text>Do you really want to delete this mod?</v-card-text>
        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="darken-1" text @click="dialog = false">No</v-btn>
          <v-btn color="primary darken-1" text @click="removeMod">Yes</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { getMods, deleteMod, showHttpErrorMessage } from "@/services";
import { logout } from "@/stores/applicationStore";
import router from "@/router";

const mods = ref([]);
const searchText = ref("");
const dialog = ref(false);
const modToDelete = ref({});

onMounted(async () => {
  updateMods();
});

const filteredMods = computed(() => {
  return mods.value.filter((m) =>
    m.name.toLowerCase()
      .includes(searchText.value.toLowerCase())
  );
});

function updateMods() {
  getMods()
    .then((response) => {
      mods.value = response;
    })
    .catch(showHttpErrorMessage);
}

function openUpdates(modID) {
  router.push({
    path: "updates",
    query: { modID: modID }
  });
}

function editMod(modID) {
  router.push({
    path: "mod",
    query: { modID: modID }
  });
}

function removeMod() {
  dialog.value = false;
  deleteMod(modToDelete.value.modID)
    .then(() => {
      updateMods();
    })
    .catch(showHttpErrorMessage);
}

function openDeleteDialog(mod) {
  dialog.value = true;
  modToDelete.value = mod;
}

function doLogout() {
  logout();
  router.push("/");
}
</script>
