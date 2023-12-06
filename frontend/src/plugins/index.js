import vuetify from './vuetify';
import router from '../router';
import { createPinia } from 'pinia';

const pinia = createPinia();

export function registerPlugins(app) {
  app.use(vuetify).use(router).use(pinia);
}
