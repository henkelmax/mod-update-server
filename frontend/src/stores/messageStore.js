import { ref } from 'vue';
import { defineStore } from 'pinia';

export const useMessageStore = defineStore('messageStore', () => {
  const errorSnackbar = ref(false);
  const errorMessage = ref('');
  const showErrorMessage = (message) => {
    errorMessage.value = message;
    errorSnackbar.value = true;
  };

  return {
    errorSnackbar,
    errorMessage,
    showErrorMessage
  };
});
