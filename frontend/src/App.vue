<template>
  <router-view />
  <v-snackbar v-model="errorSnackbar" bottom color="red" multi-line :timeout="6000">
    {{ errorMessage }}
    <template v-slot:actions>
      <v-btn dark text @click="errorSnackbar = false">Close</v-btn>
    </template>
  </v-snackbar>
</template>

<script setup>
import { onMounted } from "vue";
import { useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { isLoggedIn } from "@/stores/applicationStore";
import router from "@/router";
import { useMessageStore } from "@/stores/messageStore";

const route = useRoute();
const messageStore = useMessageStore();

const { errorSnackbar, errorMessage } = storeToRefs(messageStore);

onMounted(async () => {
  if (!isLoggedIn() && route.name !== "login") {
    router.push("");
  }
});
</script>
