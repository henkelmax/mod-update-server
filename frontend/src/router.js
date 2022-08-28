import Vue from 'vue';
import Router from 'vue-router';
import Login from './views/Login.vue';

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login
    },
    {
      path: '/mods',
      name: 'mods',
      component: () => import(/* webpackChunkName: "mods" */ './views/Mods.vue')
    },
    {
      path: '/mod',
      name: 'mod',
      props: { modID: '' },
      component: () => import(/* webpackChunkName: "mod" */ './views/Mod.vue')
    },
    {
      path: '/updates',
      name: 'updates',
      props: { modID: '' },
      component: () => import(/* webpackChunkName: "updates" */ './views/Updates.vue')
    },
    {
      path: '/update',
      name: 'update',
      props: { modID: '', updateID: '' },
      component: () => import(/* webpackChunkName: "update" */ './views/Update.vue')
    },
    {
      path: '/apikeys',
      name: 'apikeys',
      component: () => import(/* webpackChunkName: "apikeys" */ './views/ApiKeys.vue')
    },
    {
      path: '/import',
      name: 'import',
      component: () => import(/* webpackChunkName: "import" */ './views/Import.vue')
    },
    {
      path: '/backup',
      name: 'backup',
      component: () => import(/* webpackChunkName: "backup" */ './views/Backup.vue')
    },
    {
      path: '/restore',
      name: 'restore',
      component: () => import(/* webpackChunkName: "restore" */ './views/Restore.vue')
    }
  ]
});
