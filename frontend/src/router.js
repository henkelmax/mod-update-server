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
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "about" */ './views/Mods.vue')
    },
    {
      path: '/mod',
      name: 'mod',
      props: { modID: '' },
      component: () => import('./views/Mod.vue')
    },
    {
      path: '/updates',
      name: 'updates',
      props: { modID: '' },
      component: () => import('./views/Updates.vue')
    },
    {
      path: '/update',
      name: 'update',
      props: { modID: '', updateID: '' },
      component: () => import('./views/Update.vue')
    },
    {
      path: '/apikeys',
      name: 'apikeys',
      component: () => import('./views/ApiKeys.vue')
    },
    {
      path: '/import',
      name: 'import',
      component: () => import('./views/Import.vue')
    },
    {
      path: '/backup',
      name: 'backup',
      component: () => import('./views/Backup.vue')
    },
    {
      path: '/restore',
      name: 'restore',
      component: () => import('./views/Restore.vue')
    }
  ]
});
