<template>
  <el-container class="layout">
    <el-aside :width="isCollapse ? '64px' : '210px'" class="aside">
      <div class="logo">
        <el-icon :size="24" color="#409eff"><HomeFilled /></el-icon>
        <span v-show="!isCollapse">农村社区管理</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#c0c4cc"
        active-text-color="#409eff"
        router
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <template #title>{{ item.meta.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ $route.meta.title }}</span>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><UserFilled /></el-icon>
            {{ userStore.user?.realName || userStore.user?.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                角色：{{ roleText }}
              </el-dropdown-item>
              <el-dropdown-item disabled>
                数据范围：{{ scopeText }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const menus = computed(() => {
  const home = router.options.routes.find((r) => r.path === '/')
  const children = home?.children || []
  return children.filter((c) => {
    const perm = c.meta?.perm
    if (!perm) return false
    return userStore.permissions.includes(perm)
  })
})

const roleMap = {
  ADMIN: '管理员',
  COMMITTEE: '村委工作人员',
  GRID_WORKER: '网格员',
  VILLAGER: '村民'
}
const scopeMap = {
  ALL: '全网数据',
  VILLAGE: '本村数据',
  GRID: '本网格数据',
  SELF: '本人数据'
}

const roleText = computed(() => userStore.roles.map((r) => roleMap[r] || r).join('、') || '未分配')
const scopeText = computed(() => scopeMap[userStore.dataScope] || '未定义')

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background-color: #001529;
  transition: width 0.2s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.aside :deep(.el-menu) {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #303133;
  outline: none;
}

.main {
  background: #f0f2f5;
  padding: 16px;
}
</style>
