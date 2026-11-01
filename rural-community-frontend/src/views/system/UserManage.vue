<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增用户</el-button>
      </div>
    </template>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}</template>
      </el-table-column>
      <el-table-column label="所属网格" width="100">
        <template #default="{ row }">{{ row.gridId || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
    </el-table>

    <!-- 新增用户 -->
    <el-dialog v-model="addVisible" title="新增用户" width="520px" :close-on-click-modal="false">
      <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addForm.password" type="password" placeholder="初始密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="addForm.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addForm.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="addForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="addForm.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属网格">
          <el-input-number v-model="addForm.gridId" :min="1" placeholder="网格员必填" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doCreate">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserList, createUser, getRoleList } from '@/api/user'

const list = ref([])
const loading = ref(false)
const roles = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  const res = await getRoleList()
  roles.value = res.data || []
}

// 新增
const addVisible = ref(false)
const submitting = ref(false)
const addRef = ref()
const addForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  gender: 1,
  roleId: null,
  gridId: null
})
const addRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const openAdd = () => {
  Object.assign(addForm, {
    username: '', password: '', realName: '', phone: '',
    gender: 1, roleId: null, gridId: null
  })
  addVisible.value = true
}

const doCreate = async () => {
  await addRef.value.validate()
  submitting.value = true
  try {
    await createUser(addForm)
    ElMessage.success('新增成功')
    addVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
