<template>
  <v-card max-width="75%" class="mx-auto mt-4">
    <v-toolbar color="secondary">
      <v-toolbar-title>{{
          newUpdate ? "Add a new update" : `Edit ${mod.name} ${update.version}`
        }}
      </v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn text color="white" @click="back">Back</v-btn>
    </v-toolbar>
    <v-card-item>
      <v-card-title></v-card-title>
    </v-card-item>
    <v-card-text>
      <v-form ref="form" v-model="valid">
        <v-container>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="publishTime"
                label="Publish Time"
                :rules="timeRules"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="publishDate"
                label="Publish Date"
                :rules="dateRules"
                required
              ></v-text-field>
            </v-col>

            <v-col cols="12" md="12">
              <v-text-field
                v-model="update.gameVersion"
                :rules="stringRequiredRules"
                label="Game version"
                required
              ></v-text-field>
            </v-col>

            <v-col cols="12" md="12">
              <v-radio-group inline v-model="update.modLoader" :mandatory="true">
                <v-radio label="Forge" value="forge"></v-radio>
                <v-radio label="NeoForge" value="neoforge"></v-radio>
                <v-radio label="Fabric" value="fabric"></v-radio>
                <v-radio label="Quilt" value="quilt"></v-radio>
              </v-radio-group>
            </v-col>

            <v-col cols="12" md="12">
              <v-text-field
                v-model="update.version"
                :rules="stringRequiredRules"
                label="Mod version"
                required
              ></v-text-field>
            </v-col>

            <v-col cols="12" md="12">
              <v-textarea
                v-model="updateMessages"
                :rules="stringRequiredRules"
                label="Update Message"
                required
              ></v-textarea>
            </v-col>

            <v-col cols="12" md="12">
              <v-radio-group inline v-model="update.releaseType" :mandatory="true">
                <v-radio label="Alpha" value="alpha"></v-radio>
                <v-radio label="Beta" value="beta"></v-radio>
                <v-radio label="Release" value="release"></v-radio>
              </v-radio-group>
            </v-col>
            <v-col cols="12" md="12">
              <v-checkbox
                v-model="update.tags"
                value="recommended"
                label="Recommended"
              ></v-checkbox>
            </v-col>
          </v-row>
        </v-container>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn class="mr-4 mb-2" @click="back">Back</v-btn>
      <v-btn :disabled="!valid" color="green" class="mr-6 mb-2" @click="validate">{{
          newUpdate ? "Add" : "Update"
        }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { onMounted, ref, computed, watch } from "vue";
import { useRoute } from "vue-router";
import { getMod, getUpdate, addUpdate, editUpdate, showHttpErrorMessage, showErrorMessage } from "@/services";
import moment from "moment";
import router from "@/router";

const route = useRoute();

const modId = ref(route.query.modID);
const updateId = ref(route.query.updateID);

const newUpdate = ref(true);
const update = ref({
  modLoader: "forge",
  tags: [],
  publishDate: moment()
    .format(),
  releaseType: "alpha"
});
const mod = ref({});
const valid = ref(false);
const publishTime = ref(moment()
  .format("HH:mm"));
const publishDate = ref(moment()
  .format("YYYY-MM-DD"));

const stringRequiredRules = [(v) => !!v || "This field is required"];

const timeRules = [
  (v) => !!v || "This field is required",
  (v) => /^([0-1][0-9]|2[0-3]):[0-5][0-9]$/.test(v) || "Format: hh:mm"
];

const dateRules = [
  (v) => !!v || "This field is required",
  (v) =>
    /^\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$/.test(v) || "Format: yyyy-mm-dd"
];

onMounted(async () => {
  if (!modId.value || !updateId.value) {
    return;
  }
  newUpdate.value = false;

  getMod(modId.value)
    .then((response) => {
      mod.value = response;
    })
    .catch(showHttpErrorMessage);

  getUpdate(modId.value, updateId.value)
    .then((response) => {
      update.value = response;
      delete update.value._id;
      delete update.value.mod;
      publishTime.value = moment(update.value.publishDate)
        .format("HH:mm");
      publishDate.value = moment(update.value.publishDate)
        .format("YYYY-MM-DD");
    })
    .catch(showHttpErrorMessage);
});

const updateMessages = computed({
  get() {
    if (!update.value.updateMessages) {
      return "";
    }
    return update.value.updateMessages.join("\n");
  },
  set(value) {
    update.value.updateMessages = value.split("\n");
  }
});

watch(publishTime, async (time) => {
  const val = moment(time, "HH:mm");
  update.value.publishDate = moment(update.value.publishDate)
    .set({
      hour: val.hour(),
      minute: val.minute(),
      second: 0,
      millisecond: 0
    })
    .format();
});

watch(publishDate, async (date) => {
  const val = moment(date, "YYYY-MM-DD");
  update.value.publishDate = moment(update.value.publishDate)
    .set({
      date: val.date(),
      month: val.month(),
      year: val.year()
    })
    .format();
});

function validate() {
  if (!valid.value) {
    showErrorMessage("Please check your fields");
    return;
  }

  if (!modId.value) {
    return;
  }

  if (newUpdate.value) {
    addUpdate(modId.value, update.value)
      .then(() => {
        router.push({
          path: "updates",
          query: { modID: modId.value }
        });
      })
      .catch(showHttpErrorMessage);
  } else {
    editUpdate(modId.value, updateId.value, update.value)
      .then(() => {
        router.push({
          path: "updates",
          query: { modID: modId.value }
        });
      })
      .catch(showHttpErrorMessage);
  }
}

function back() {
  router.go(-1);
}
</script>
