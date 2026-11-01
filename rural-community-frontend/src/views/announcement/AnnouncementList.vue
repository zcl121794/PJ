<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>公告列表</span>
        <div v-if="canPublish" class="actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">发布公告</el-button>
        </div>
      </div>
    </template>

    <div class="filter">
      <el-input v-model="query.keyword" placeholder="搜索标题" clearable style="width: 200px" @keyup.enter="handleSearch" />
      <el-select v-model="query.category" placeholder="分类" clearable style="width: 150px">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="可见范围" width="100">
        <template #default="{ row }">
          {{ row.publishType === 2 ? '网格定向' : '全村' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="danger" size="small">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveTime" label="生效时间" width="170" />
      <el-table-column prop="readCount" label="阅读量" width="90" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button v-if="canEditRow(row)" link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="canWithdrawRow(row)" link type="danger" @click="handleWithdraw(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="loadData"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="viewVisible" title="公告详情" width="640px">
      <h3>{{ current.title }}</h3>
      <div class="meta">
        <el-tag size="small">{{ current.category }}</el-tag>
        <span>阅读量 {{ current.readCount }}</span>
        <span>生效时间 {{ current.effectiveTime }}</span>
      </div>
      <div class="content">{{ current.content }}</div>
    </el-dialog>

    <!-- 发布/编辑公告 -->
    <el-dialog v-model="addVisible" :title="editingId ? '编辑公告' : '发布公告'" width="620px" :close-on-click-modal="false">
      <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="addForm.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="addForm.category" placeholder="请选择分类" clearable style="width: 100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="addForm.content" type="textarea" :rows="5" placeholder="公告正文" />
        </el-form-item>
        <el-form-item label="可见范围">
          <el-radio-group v-model="addForm.publishType">
            <el-radio :label="1">全村</el-radio>
            <el-radio :label="2">网格定向</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="addForm.publishType === 2" label="网格ID">
          <el-input-number v-model="addForm.gridId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="addForm.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="addForm.effectiveTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择生效时间（可选）" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button @click="doSave(0)">存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="doSave(2)">发布</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getAnnouncementList, createAnnouncement, updateAnnouncement, withdrawAnnouncement
} from '@/api/announcement'
import { getDictByType } from '@/api/dict'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const canPublish = computed(() => userStore.permissions.includes('announcement:add'))
const isManager = computed(() => userStore.roles.some((r) => r === 'ADMIN' || r === 'COMMITTEE'))

// 编辑：草稿/待审核/已下架；已发布需先下架；网格员仅能编辑本人发布的公告
const canEditRow = (row) =>
  userStore.permissions.includes('announcement:edit') &&
  [0, 1, 3].includes(row.status) &&
  (isManager.value || row.publishBy === userStore.user?.id)
// 下架：仅已发布公告，村委/管理员可操作
const canWithdrawRow = (row) =>
  userStore.permissions.includes('announcement:remove') &&
  row.status === 2 &&
  (isManager.value || row.publishBy === userStore.user?.id)

const list = ref([])
const loading = ref(false)
const total = ref(0)
const viewVisible = ref(false)
const current = ref({})
const categories = ref([])
const addVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const addRef = ref()
const addForm = reactive({
  title: '', category: '', content: '', publishType: 1, gridId: null, isTop: 0, effectiveTime: ''
})
const addRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const statusMap = { 0: '草稿', 1: '待审核', 2: '已发布', 3: '已撤回', 4: '已归档' }
const statusType = (s) => (s === 2 ? 'success' : s === 1 ? 'warning' : s === 0 ? 'info' : 'danger')
const statusText = (s) => statusMap[s] || '未知'

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await getDictByType('announcement_category')
  categories.value = (res.data || []).map((d) => d.dictLabel)
}

const handleSearch = () => {
  query.pageNum = 1
  loadData()
}

const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

const handleView = (row) => {
  current.value = row
  viewVisible.value = true
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(addForm, {
    title: '', category: '', content: '', publishType: 1, gridId: null, isTop: 0, effectiveTime: ''
  })
  addVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(addForm, {
    title: row.title || '',
    category: row.category || '',
    content: row.content || '',
    publishType: row.publishType ?? 1,
    gridId: row.gridId ?? null,
    isTop: row.isTop ?? 0,
    effectiveTime: row.effectiveTime || ''
  })
  addVisible.value = true
}

const doSave = async (status) => {
  await addRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      ...addForm,
      status,
      effectiveTime: addForm.effectiveTime || null
    }
    if (editingId.value) {
      await updateAnnouncement(editingId.value, payload)
    } else {
      await createAnnouncement(payload)
    }
    ElMessage.success(status === 2 ? '发布成功' : '已保存草稿')
    addVisible.value = false
    editingId.value = null
    handleSearch()
  } finally {
    submitting.value = false
  }
}

const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定下架公告「${row.title}」吗？下架后村民将无法查看，可编辑后重新发布。`,
      '下架公告',
      { type: 'warning', confirmButtonText: '下架', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await withdrawAnnouncement(row.id)
  ElMessage.success('已下架，可点击编辑修改后重新发布')
  loadData()
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.meta {
  display: flex;
  gap: 16px;
  align-items: center;
  margin: 12px 0;
  color: #909399;
  font-size: 13px;
}

.content {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
