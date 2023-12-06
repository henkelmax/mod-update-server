<template>
  <router-view />
</template>

<script setup>
import { onMounted } from "vue";
import { useRoute } from "vue-router";
import { isLoggedIn } from "@/stores/application";
import router from "@/router";

const route = useRoute();

onMounted(async () => {
  if (!isLoggedIn() && route.name !== "login") {
    router.push("");
  }
});
</script>
