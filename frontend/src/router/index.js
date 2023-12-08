import { createRouter, createWebHistory } from 'vue-router'
import Default from '@/layouts/default/Default.vue'
import Login from '@/views/Login.vue'
import Mods from '@/views/Mods.vue'
import Mod from '@/views/Mod.vue'
import Updates from '@/views/Updates.vue'
import Update from '@/views/Update.vue'
import Import from '@/views/Import.vue'
import Backup from '@/views/Backup.vue'
import Restore from '@/views/Restore.vue'
import ApiKeys from '@/views/ApiKeys.vue'
import LatestUpdates from '@/views/LatestUpdates.vue'

const routes = [
  {
    path: '/',
    component: Default,
    children: [
      {
        path: '',
        name: 'login',
        component: Login,
      },
      {
        path: 'mods',
        name: 'mods',
        component: Mods,
      },
      {
        path: 'mod',
        name: 'mod',
        component: Mod,
      },
      {
        path: 'updates',
        name: 'updates',
        params: { modId: '' },
        component: Updates,
      },
      {
        path: 'update',
        name: 'update',
        params: { modId: '', updateId: '' },
        component: Update,
      },
      {
        path: 'import',
        name: 'import',
        component: Import,
      },
      {
        path: 'backup',
        name: 'backup',
        component: Backup,
      },
      {
        path: 'restore',
        name: 'restore',
        component: Restore,
      },
      {
        path: 'apikeys',
        name: 'apikeys',
        component: ApiKeys,
      },
      {
        path: 'latest',
        name: 'latest',
        component: LatestUpdates,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
