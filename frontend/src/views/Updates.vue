<template>
  <v-card max-width="75%" class="mx-auto mt-4">
    <v-toolbar color="secondary">
      <v-toolbar-title>Updates of {{ mod.name }}</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn text color="white" @click="addUpdate">Add Update</v-btn>
      <v-btn text color="white" @click="back">Back</v-btn>
    </v-toolbar>
    <template v-if="updates.length > 0">
      <v-list lines="two">
        <template v-for="(update, i) in updates" :key="update._id">
          <v-list-item>
            <v-list-item-title class="text-h6">
              {{ mod.name }} {{ update.version }}
              <v-chip class="ml-2" variant="flat" color="blue" size="small" label>
                {{ update.gameVersion }}
              </v-chip>
              <v-chip class="ml-1" variant="flat" color="blue" size="small" label>
                {{ update.modLoader }}
              </v-chip>
              <v-chip
                v-for="tag in update.tags"
                :key="tag"
                class="ml-1"
                variant="flat"
                color="green"
                size="small"
                label
              >
                {{ tag }}
              </v-chip>
            </v-list-item-title>
            <div class="mb-1 text-grey-lighten-1 text-subtitle-2">
              {{ `Released ${moment(update.publishDate).fromNow()}` }}
            </div>
            <div v-for="(str, idx) in update.updateMessages" :key="idx" class="text-grey">
              {{ str }}
            </div>
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
      <div class="text-center">
        <v-pagination v-model="page" :length="pages"></v-pagination>
      </div>
    </template>
    <template v-else>
      <v-card-text class="text-center">
        This mod does not have any updates yet
      </v-card-text>
    </template>
  </v-card>
  <v-container>
    <v-dialog v-model="dialog" max-width="35%">
      <v-card>
        <v-toolbar color="secondary">
          <v-toolbar-title>Delete update?</v-toolbar-title>
        </v-toolbar>
        <v-card-text
          >Do you really want to delete
          {{
            `"[${updateToDelete.gameVersion}] ${mod.name} ${updateToDelete.version}"`
          }}?</v-card-text
        >
        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="red" text @click="dialog = false">No</v-btn>
          <v-btn color="green" text @click="remove">Yes</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { onMounted, ref, computed, watch } from "vue";
import { useRoute } from "vue-router";
import { getMod, getUpdates, removeUpdate } from "@/services";
import { showHttpErrorMessage } from "@/services/messages";
import router from "@/router";
import moment from "moment";

const route = useRoute();

const modId = ref(route.query.modID);
const mod = ref({});
const updates = ref([]);
const page = ref(1);
const amountPerPage = ref(16);
const dialog = ref(false);
const updateToDelete = ref({});

onMounted(async () => {
  if (!modId.value) {
    router.go(-1);
    return;
  }

  getMod(modId.value)
    .then((response) => {
      mod.value = response;
    })
    .catch(showHttpErrorMessage);
  updateUpdates();
});

const pages = computed(() => {
  return mod.value.updateCount
    ? Math.ceil(mod.value.updateCount / amountPerPage.value)
    : 1;
});

watch(page, () => {
  updateUpdates();
});

function updateUpdates() {
  getUpdates(modId.value, amountPerPage.value, page.value - 1)
    .then((response) => {
      updates.value = response;
    })
    .catch(showHttpErrorMessage);
}

function remove() {
  dialog.value = false;
  removeUpdate(mod.value.modID, updateToDelete.value._id)
    .then(() => {
      updateUpdates();
    })
    .catch(showHttpErrorMessage);
}

function openDeleteDialog(update) {
  dialog.value = true;
  updateToDelete.value = update;
}

function editUpdate(updateID) {
  router.push({
    path: "update",
    query: { modID: modId.value, updateID: updateID },
  });
}

function addUpdate() {
  router.push({
    path: "update",
    query: { modID: modId.value },
  });
}

function back() {
  router.push("mods");
}
</script>
