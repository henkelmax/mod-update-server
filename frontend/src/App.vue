<template>
  <v-app>
    <v-app-bar app>
      <v-toolbar-title class="headline text-uppercase">
        <span>Update Manager</span>
      </v-toolbar-title>

      <v-spacer></v-spacer>
      <v-btn v-if="loggedIn()" text @click="apiKeys">
        <span class="mr-2">API Keys</span>
        <v-icon>mdi-lock</v-icon>
      </v-btn>
      <v-btn v-if="loggedIn()" text @click="logOut">
        <span class="mr-2">Logout</span>
        <v-icon>mdi-power</v-icon>
      </v-btn>
    </v-app-bar>

    <v-main>
      <router-view/>
    </v-main>
  </v-app>
</template>

<script>
export default {
  name: "App",
  data: () => ({
    //
  }),
  created() {
    if (!sessionStorage.apiKey && this.$route.name !== "login") {
      this.$router.push({ path: "/" });
    }
  },
  methods: {
    apiKeys() {
      if (this.$route.name !== "apikeys") {
        this.$router.push({ path: "apikeys" });
      }
    },
    loggedIn() {
      return !!sessionStorage.apiKey;
    },
    logOut() {
      sessionStorage.removeItem("apiKey");
      if (this.$route.name !== "login") {
        this.$router.push({ path: "/" });
      }
    }
  }
};
</script>
