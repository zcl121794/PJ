import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/login',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataLine', perm: 'dashboard:view' }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/announcement/AnnouncementList.vue'),
        meta: { title: '公告管理', icon: 'Bell', perm: 'announcement:list' }
      },
      {
        path: 'villager',
        name: 'Villager',
        component: () => import('@/views/villager/VillagerList.vue'),
        meta: { title: '村民信息', icon: 'User', perm: 'villager:list' }
      },
      {
        path: 'appeal',
        name: 'Appeal',
        component: () => import('@/views/appeal/AppealList.vue'),
        meta: { title: '诉求反馈', icon: 'ChatDotRound', perm: 'appeal:list' }
      },
      {
        path: 'service',
        name: 'Service',
        component: () => import('@/views/service/ServiceGuide.vue'),
        meta: { title: '办事服务', icon: 'Document', perm: 'service:guide' }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '权限管理', icon: 'Setting', perm: 'system:user' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
