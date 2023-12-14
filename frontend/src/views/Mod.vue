<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-toolbar color="secondary">
        <v-toolbar-title>{{
            newMod ? "Add a new mod" : `Edit ${mod.name}`
          }}
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn text color="white" @click="back">Back</v-btn>
      </v-toolbar>
      <v-card-text>
        <v-form v-model="valid">
          <v-container>
            <v-row>
              <v-col v-if="newMod" cols="12" md="12">
                <v-text-field
                  v-model="mod.modID"
                  :rules="modIDRules"
                  label="Mod ID"
                  required
                ></v-text-field>
              </v-col>

              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.name"
                  :rules="stringRequiredRules"
                  label="Mod name"
                  required
                ></v-text-field>
              </v-col>

              <v-col cols="12" md="12">
                <v-textarea
                  v-model="mod.description"
                  :rules="stringRequiredRules"
                  label="Description"
                  required
                ></v-textarea>
              </v-col>

              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.websiteURL"
                  :rules="stringRequiredRules"
                  label="Website URL"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.downloadURL"
                  :rules="stringRequiredRules"
                  label="Download URL"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="12">
                <v-text-field
                  v-model="mod.issueURL"
                  :rules="stringRequiredRules"
                  label="Issue Tracker URL"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn class="mr-4 mb-2" @click="back">Back</v-btn>
        <v-btn :disabled="!valid" color="green" class="mr-6 mb-2" @click="validate">{{
            newMod ? "Add" : "Update"
          }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { getMod, addMod, editMod, showHttpErrorMessage, showErrorMessage } from "@/services";
import router from "@/router";

const route = useRoute();

const modId = ref(route.query.modID);

const newMod = ref(true);
const mod = ref({});
const valid = ref(false);

const modIDRules = [
  (v) => !!v || "This field is required",
  (v) => /^[a-z-_]*$/.test(v) || "The Mod ID can only contain [a-z]"
];

const stringRequiredRules = [(v) => !!v || "This field is required"];

onMounted(async () => {
  if (!modId.value) {
    return;
  }
  newMod.value = false;

  getMod(modId.value)
    .then((response) => {
      mod.value = response;
    })
    .catch(showHttpErrorMessage);
});

function validate() {
  if (!valid.value) {
    showErrorMessage("Please check your fields");
    return;
  }

  if (newMod.value) {
    addMod(mod.value)
      .then(() => {
        back();
      })
      .catch(showHttpErrorMessage);
  } else {
    const updatedMod = {
      name: mod.value.name,
      description: mod.value.description,
      websiteURL: mod.value.websiteURL,
      downloadURL: mod.value.downloadURL,
      issueURL: mod.value.issueURL
    };
    editMod(mod.value.modID, updatedMod)
      .then(() => {
        back();
      })
      .catch(showHttpErrorMessage);
  }
}

function back() {
  router.push("mods");
}
</script>
