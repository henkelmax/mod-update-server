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
            @keyup.enter="logIn()"
            required
          ></v-text-field>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn class="mr-6 mb-2" @click="logIn()" :disabled="!valid">Log In</v-btn>
      </v-card-actions>
    </v-card>
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
      password: "",
      valid: false,
      error: "",
      snackbar: false,
      passwordRules: [
        v => !!v || "This field is required",
        v =>
          /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(
            v
          ) || "The API key has to be in the UUID format"
      ]
    };
  },
  created() {
    if (!!sessionStorage.apiKey) {
      this.$router.push("mods");
    }
  },
  methods: {
    logIn() {
      if (!this.$refs.form.validate()) {
        this.snackbar = true;
        this.error = "Please check your fields";
        return;
      }
      if (this.password.trim() === "") {
        return;
      }
      sessionStorage.apiKey = this.password;
      this.$router.push("mods");
    }
  }
};
</script>
