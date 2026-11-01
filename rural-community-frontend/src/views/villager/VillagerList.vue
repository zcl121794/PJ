<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>村民信息</span>
        <div class="actions">
          <el-button :icon="Upload" @click="handleImport">批量导入</el-button>
          <el-button type="primary" :icon="Plus" @click="openAdd">新增村民</el-button>
        </div>
      </div>
    </template>

    <div class="filter">
      <el-input v-model="query.name" placeholder="姓名" clearable style="width: 150px" @keyup.enter="handleSearch" />
      <el-input v-model="query.idCard" placeholder="身份证号" clearable style="width: 200px" @keyup.enter="handleSearch" />
      <el-select v-model="query.specialTag" placeholder="特殊群体" clearable style="width: 140px">
        <el-option v-for="t in specialTags" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="户号" width="110" fixed>
        <template #default="{ row }">{{ householdMap[row.householdId] || row.householdId }}</template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="90" fixed />
      <el-table-column label="性别" width="60">
        <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}</template>
      </el-table-column>
      <el-table-column prop="idCard" label="身份证号" width="170" />
      <el-table-column prop="birthDate" label="出生日期" width="110" />
      <el-table-column prop="nation" label="民族" width="80" />
      <el-table-column prop="education" label="学历" width="80" />
      <el-table-column prop="politicalStatus" label="政治面貌" width="90" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="healthStatus" label="健康状况" width="80" />
      <el-table-column prop="employmentStatus" label="就业状态" width="90" />
      <el-table-column prop="specialTag" label="特殊群体" min-width="130">
        <template #default="{ row }">
          <el-tag v-if="row.specialTag" type="warning" size="small">{{ row.specialTag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否户主" width="80">
        <template #default="{ row }">{{ row.isHouseholder === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="relation" label="与户主关系" width="100" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="can('villager:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="can('villager:remove')" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增村民 -->
    <el-dialog v-model="addVisible" :title="dialogMode === 'edit' ? '编辑村民' : '新增村民'" width="640px" :close-on-click-modal="false">
      <el-form ref="addRef" :model="addForm" :rules="addRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属户" prop="householdId">
              <el-select v-model="addForm.householdId" placeholder="请选择户" style="width: 100%">
                <el-option v-for="h in households" :key="h.id" :label="h.householdNo" :value="h.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="addForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="addForm.idCard" placeholder="18 位身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="addForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="addForm.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="addForm.nation" placeholder="民族" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="addForm.education" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="d in educations" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌">
              <el-select v-model="addForm.politicalStatus" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="d in politicalStatuses" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="addForm.phone" placeholder="手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康状况">
              <el-input v-model="addForm.healthStatus" placeholder="健康状况" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="就业状态">
              <el-select v-model="addForm.employmentStatus" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="d in employmentStatuses" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="特殊群体">
              <el-select v-model="addForm.specialTag" multiple placeholder="可多选" style="width: 100%">
                <el-option v-for="t in specialTags" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否户主">
              <el-switch v-model="addForm.isHouseholder" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="与户主关系">
              <el-input v-model="addForm.relation" placeholder="如：配偶、子、女" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="addForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入 -->
    <el-dialog v-model="importVisible" title="批量导入村民" width="560px">
      <el-alert type="info" :closable="false" title="Excel 模板列：户号、姓名、身份证号、性别、出生日期、民族、学历、政治面貌、联系电话、健康状况、就业状态、特殊群体、是否户主、与户主关系、备注" class="import-alert" />
      <el-upload
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="onFileChange"
        :on-remove="onFileRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击选择</em></div>
      </el-upload>
      <div v-if="importResult" class="import-result">
        <p>成功 {{ importResult.success }} 条，失败 {{ importResult.fail }} 条</p>
        <ul v-if="importResult.errors.length" class="error-list">
          <li v-for="(e, i) in importResult.errors" :key="i">{{ e }}</li>
        </ul>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getVillagerList, createVillager, updateVillager, deleteVillager, importVillagers } from '@/api/villager'
import { getHouseholdList } from '@/api/household'
import { getDictByType } from '@/api/dict'

const userStore = useUserStore()

const list = ref([])
const loading = ref(false)
const total = ref(0)
const specialTags = ref([])
const educations = ref([])
const politicalStatuses = ref([])
const employmentStatuses = ref([])
const households = ref([])
const householdMap = computed(() => {
  const map = {}
  households.value.forEach((h) => { map[h.id] = h.householdNo })
  return map
})

const query = reactive({ pageNum: 1, pageSize: 10, name: '', idCard: '', specialTag: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getVillagerList(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const loadDict = async (type, target) => {
  const res = await getDictByType(type)
  target.value = (res.data || []).map((d) => d.dictLabel)
}

const handleSearch = () => {
  query.pageNum = 1
  loadData()
}

const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

// 新增 / 编辑
const dialogMode = ref('add')
const editId = ref(null)
const addVisible = ref(false)
const submitting = ref(false)
const addRef = ref()
const addForm = reactive({
  householdId: null,
  name: '',
  idCard: '',
  gender: 1,
  birthDate: '',
  nation: '汉族',
  education: '',
  politicalStatus: '群众',
  phone: '',
  healthStatus: '健康',
  employmentStatus: '',
  specialTag: [],
  isHouseholder: 0,
  relation: '本人',
  remark: ''
})
const addRules = {
  householdId: [{ required: true, message: '请选择所属户', trigger: 'change' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

const openAdd = async () => {
  dialogMode.value = 'add'
  editId.value = null
  Object.assign(addForm, {
    householdId: null, name: '', idCard: '', gender: 1, birthDate: '', nation: '汉族',
    education: '', politicalStatus: '群众', phone: '', healthStatus: '健康',
    employmentStatus: '', specialTag: [], isHouseholder: 0, relation: '本人', remark: ''
  })
  addVisible.value = true
  if (!households.value.length) {
    const res = await getHouseholdList()
    households.value = res.data || []
  }
}

const openEdit = async (row) => {
  dialogMode.value = 'edit'
  editId.value = row.id
  Object.assign(addForm, {
    householdId: row.householdId,
    name: row.name,
    idCard: row.idCard,
    gender: row.gender || 1,
    birthDate: row.birthDate || '',
    nation: row.nation || '汉族',
    education: row.education || '',
    politicalStatus: row.politicalStatus || '群众',
    phone: row.phone || '',
    healthStatus: row.healthStatus || '健康',
    employmentStatus: row.employmentStatus || '',
    specialTag: row.specialTag ? row.specialTag.split(',') : [],
    isHouseholder: row.isHouseholder || 0,
    relation: row.relation || '本人',
    remark: row.remark || ''
  })
  addVisible.value = true
  if (!households.value.length) {
    const res = await getHouseholdList()
    households.value = res.data || []
  }
}

const doCreate = async () => {
  await addRef.value.validate()
  submitting.value = true
  try {
    const data = {
      ...addForm,
      specialTag: addForm.specialTag.join(','),
      birthDate: addForm.birthDate || null
    }
    if (dialogMode.value === 'edit') {
      await updateVillager(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createVillager(data)
      ElMessage.success('新增成功')
    }
    addVisible.value = false
    handleSearch()
  } finally {
    submitting.value = false
  }
}

const can = (perm) => userStore.permissions.includes(perm)

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除村民「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteVillager(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const importVisible = ref(false)
const importing = ref(false)
const importFile = ref(null)
const importResult = ref(null)

const handleImport = () => {
  importFile.value = null
  importResult.value = null
  importVisible.value = true
}

const onFileChange = (file) => {
  importFile.value = file.raw
}

const onFileRemove = () => {
  importFile.value = null
}

const doImport = async () => {
  if (!importFile.value) return ElMessage.warning('请先选择 Excel 文件')
  importing.value = true
  try {
    const res = await importVillagers(importFile.value)
    importResult.value = res.data
    ElMessage.success(`导入完成：成功 ${res.data.success} 条，失败 ${res.data.fail} 条`)
    if (res.data.success > 0) handleSearch()
  } finally {
    importing.value = false
  }
}

const loadHouseholds = async () => {
  const res = await getHouseholdList()
  households.value = res.data || []
}

onMounted(() => {
  loadData()
  loadHouseholds()
  loadDict('special_tag', specialTags)
  loadDict('education', educations)
  loadDict('political_status', politicalStatuses)
  loadDict('employment_status', employmentStatuses)
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
  flex-wrap: wrap;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.import-alert {
  margin-bottom: 16px;
}

.import-result {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
}

.error-list {
  margin-top: 8px;
  padding-left: 20px;
  color: #f56c6c;
  line-height: 1.8;
}
</style>
