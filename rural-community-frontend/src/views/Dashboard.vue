<template>
  <div>
    <el-card class="welcome">
      <div class="welcome-inner">
        <el-icon :size="48" color="#409eff"><UserFilled /></el-icon>
        <div>
          <h2>你好，{{ userStore.user?.realName || userStore.user?.username }}</h2>
          <p class="sub">角色：{{ roleText }} ｜ 数据范围：{{ scopeText }}</p>
        </div>
      </div>
    </el-card>

    <el-card class="section">
      <template #header>我的权限</template>
      <div class="perm-tags">
        <el-tag v-for="p in userStore.permissions" :key="p" type="info" class="perm-tag">{{ permMap[p] || p }}</el-tag>
        <span v-if="!userStore.permissions.length" class="empty">暂无权限</span>
      </div>
    </el-card>

    <el-card class="section">
      <template #header>系统说明</template>
      <p class="desc">
        本系统面向农村基层治理，覆盖公告管理、村民信息、诉求反馈、办事服务、权限管理与数据统计六大模块。
        当前登录账号为「{{ roleText }}」，可访问菜单及数据范围由后端 RBAC 与三级数据权限动态控制。
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const roleMap = {
  ADMIN: '管理员',
  COMMITTEE: '村委工作人员',
  GRID_WORKER: '网格员',
  VILLAGER: '村民'
}
const scopeMap = { ALL: '全网数据', VILLAGE: '本村数据', GRID: '本网格数据', SELF: '本人数据' }

const permMap = {
  'announcement:list': '公告查询',
  'announcement:add': '公告新增',
  'announcement:edit': '公告编辑',
  'announcement:remove': '公告删除',
  'announcement:audit': '公告审核',
  'announcement:category': '公告分类',
  'villager:household': '户信息管理',
  'villager:household:add': '户新增',
  'villager:household:edit': '户编辑',
  'villager:household:remove': '户删除',
  'villager:list': '村民档案',
  'villager:add': '村民新增',
  'villager:edit': '村民编辑',
  'villager:remove': '村民删除',
  'villager:import': '导入导出',
  'appeal:list': '诉求查询',
  'appeal:accept': '诉求受理',
  'appeal:assign': '诉求分派',
  'appeal:handle': '诉求处理',
  'appeal:finish': '诉求办结',
  'appeal:category': '诉求分类',
  'service:guide': '办事指南',
  'service:apply': '在线申办',
  'service:accept': '申办受理',
  'service:audit': '申办审核',
  'service:finish': '申办办结',
  'service:item': '事项管理',
  'system:user': '用户管理',
  'system:role': '角色管理',
  'system:menu': '菜单管理',
  'dashboard:view': '数据看板'
}

const roleText = computed(() => userStore.roles.map((r) => roleMap[r] || r).join('、') || '未分配')
const scopeText = computed(() => scopeMap[userStore.dataScope] || '未定义')
</script>

<style scoped>
.welcome-inner {
  display: flex;
  align-items: center;
  gap: 20px;
}

.sub {
  margin-top: 8px;
  color: #909399;
}

.section {
  margin-top: 16px;
}

.perm-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.perm-tag {
  cursor: default;
}

.empty {
  color: #909399;
}

.desc {
  color: #606266;
  line-height: 1.8;
}
</style>
