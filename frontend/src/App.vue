<template>
  <v-app>
    <v-app-bar app>
      <v-toolbar-title class="headline text-uppercase">
        <span>Updates</span>
      </v-toolbar-title>
      <!--
      <v-spacer></v-spacer>
      <v-btn
        text
        href="https://maxhenkel.de/"
      >
        <span class="mr-2">Source</span>
        <v-icon>mdi-open-in-new</v-icon>
      </v-btn>
      -->
    </v-app-bar>

    <v-content>
      <router-view />
    </v-content>
  </v-app>
</template>

<script>
export default {
  name: "App",
  data: () => ({
    //
  })
};
</script>
