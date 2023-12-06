<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>Log In</span>
      </v-card-title>
      <v-card-text>
        <v-form ref="form" v-model="valid" lazy-validation>
          <v-text-field
            v-model="password"
            label="API Key"
            type="password"
            class="ml-4 mr-4"
            :rules="passwordRules"
            @keyup.enter="login"
            required
          ></v-text-field>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn class="mr-4 mb-2" @click="skip">Skip</v-btn>
        <v-btn class="mr-6 mb-2" @click="login" :disabled="!valid">Log In</v-btn>
      </v-card-actions>
    </v-card>
    <v-snackbar v-model="snackbar" bottom color="error" multi-line :timeout="6000">
      {{ error }}
      <v-btn dark text @click="snackbar = false">Close</v-btn>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { setApiKey, isLoggedIn, skipLogin } from "@/stores/application";
import { onMounted, ref } from "vue";
import router from "@/router";

const password = ref("");
const valid = ref(false);
const error = ref("");
const snackbar = ref(false);
const passwordRules = [
  (v) => !!v || "This field is required",
  (v) =>
    /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(
      v
    ) || "The API key has to be in the UUID format",
];

onMounted(() => {
  if (isLoggedIn()) {
    router.push("mods");
  }
});

function login() {
  if (!valid.value) {
    snackbar.value = true;
    error.value = "Please check your fields";
    return;
  }
  if (password.value.trim() === "") {
    return;
  }
  setApiKey(password.value);
  router.push("mods");
}

function skip() {
  skipLogin();
  router.push("mods");
}
</script>
