<template>
  <v-container>
    <v-card max-width="75%" class="mx-auto mt-4">
      <v-card-title>
        <span>Updates of {{ mod.name }}</span>
        <v-spacer></v-spacer>
        <v-btn text color="primary" @click="addUpdate()">Add Update</v-btn>
        <v-btn text color="gray" @click="back()">Back</v-btn>
      </v-card-title>
      <v-list>
        <template v-for="(update, i) in updates">
          <div :key="update._id">
            <v-list-item>
              <v-list-item-content>
                <v-list-item-title
                  v-text="
                    `[${update.gameVersion}] ${mod.name} ${update.version}`
                  "
                ></v-list-item-title>
                <v-list-item-subtitle
                  v-text="`Released ${moment(update.publishDate).fromNow()}`"
                ></v-list-item-subtitle>
                <v-list-item-subtitle
                  v-text="update.updateMessages.join('\n')"
                ></v-list-item-subtitle>
              </v-list-item-content>
              <v-list-item-action>
                <v-btn text icon color="gray" @click="editUpdate(update._id)">
                  <v-icon>mdi-pencil</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-action>
                <v-btn text icon color="gray" @click="openDeleteDialog(update)">
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
            <v-divider
              v-if="i !== updates.length - 1"
              class="ml-2 mr-2"
            ></v-divider>
          </div>
        </template>
      </v-list>
      <div class="text-center">
        <v-pagination v-model="page" :length="pages"></v-pagination>
      </div>
    </v-card>
    <v-dialog v-model="dialog" max-width="35%">
      <v-card>
        <v-card-title class="headline"
          >Delete
          {{
            `[${updateToDelete.gameVersion}] ${mod.name} ${updateToDelete.version}`
          }}</v-card-title
        >
        <v-card-text>Do you really want to delete this update?</v-card-text>
        <v-card-actions>
          <div class="flex-grow-1"></div>
          <v-btn color="darken-1" text @click="dialog = false">No</v-btn>
          <v-btn color="primary darken-1" text @click="removeUpdate">Yes</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
      multi-line
      :timeout="6000"
    >
      {{ error }}
      <v-btn dark text @click="snackbar = false">Close</v-btn>
    </v-snackbar>
  </v-container>
</template>

<script>
import axios from "axios";
import moment from "moment";

export default {
  components: {},
  data() {
    return {
      mod: {},
      updates: [],
      page: 1,
      amountPerPage: 16,
      dialog: false,
      updateToDelete: {},
      snackbar: false,
      error: ""
    };
  },
  created() {
    if (!this.$route.query.modID) {
      this.$router.push(-1);
      return;
    }

    axios
      .get(`${this.server}/mods/${this.$route.query.modID}`)
      .then((response) => {
        this.mod = response.data;
      })
      .catch((err) => {
        this.error = "Could not retrieve mods";
        this.snackbar = true;
      });
    this.updateUpdates();
  },
  methods: {
    updateUpdates() {
      axios
        .get(
          `${this.server}/updates/${this.$route.query.modID}?amount=${
            this.amountPerPage
          }&page=${this.page - 1}`
        )
        .then((response) => {
          this.updates = response.data;
        })
        .catch((err) => {
          this.error = "Could not retrieve updates";
          this.snackbar = true;
        });
    },
    removeUpdate() {
      this.dialog = false;
      axios
        .delete(
          `${this.server}/updates/${this.mod.modID}/${this.updateToDelete._id}`,
          {
            headers: {
              apikey: sessionStorage.apiKey
            }
          }
        )
        .then((response) => {
          this.updateUpdates();
        })
        .catch((err) => {
          this.error = "Could not delete update";
          this.snackbar = true;
        });
    },
    openDeleteDialog(update) {
      this.dialog = true;
      this.updateToDelete = update;
    },
    editUpdate(updateID) {
      this.$router.push({
        path: "update",
        query: { modID: this.$route.query.modID, updateID: updateID }
      });
    },
    addUpdate() {
      this.$router.push({
        path: "update",
        query: { modID: this.$route.query.modID }
      });
    },
    back() {
      this.$router.push({
        path: "mods"
      });
    },
    moment
  },
  computed: {
    pages() {
      return this.mod.updateCount
        ? Math.ceil(this.mod.updateCount / this.amountPerPage)
        : 1;
    }
  },
  watch: {
    page(page) {
      this.updateUpdates();
    }
  }
};
</script>
