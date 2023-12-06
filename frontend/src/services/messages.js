import { useMessageStore } from '@/stores/messageStore';
import { getErrorMessage } from '@/services';

export function showErrorMessage(message) {
  useMessageStore().showErrorMessage(message);
}

export function showHttpErrorMessage(err) {
  useMessageStore().showErrorMessage(getErrorMessage(err));
}
