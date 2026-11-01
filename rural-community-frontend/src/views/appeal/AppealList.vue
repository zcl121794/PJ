<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>诉求反馈</span>
          <el-button v-if="!isAdmin" type="primary" :icon="Plus" @click="openSubmit">提交诉求</el-button>
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
        <el-table-column prop="appealNo" label="编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isGridWorker" label="满意度" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.satisfaction" :type="row.satisfaction === 1 ? 'success' : 'warning'" size="small">
              {{ satisfactionText(row.satisfaction) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="380" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="can('appeal:accept') && row.status === 0" link type="success" @click="doAccept(row)">受理</el-button>
            <el-button v-if="can('appeal:assign') && row.status === 1" link type="warning" @click="openAssign(row)">分派</el-button>
            <el-button v-if="canDirectHandle(row)" link type="primary" @click="openHandle(row)">直接处理</el-button>
            <el-button v-if="canFillResult(row)" link type="primary" @click="openHandle(row)">填写处理结果</el-button>
            <el-button v-if="canReport(row)" link type="warning" @click="openReport(row)">上报村委</el-button>
            <el-button v-if="canUrge(row)" link type="warning" @click="openUrge(row)">催办</el-button>
            <el-button v-if="canAudit(row)" link type="success" @click="doApprove(row)">审核通过</el-button>
            <el-button v-if="canAudit(row)" link type="danger" @click="openAuditReturn(row)">退回</el-button>
            <el-button v-if="canEditResult(row)" link type="primary" @click="openEditResult(row)">修改处理结果</el-button>
            <el-button v-if="canFeedback(row)" link type="success" @click="doSatisfied(row)">满意</el-button>
            <el-button v-if="canFeedback(row)" link type="danger" @click="openDissatisfied(row)">不满意</el-button>
            <el-button v-if="canFeedback(row)" link type="primary" @click="openFollowUp(row)">追问</el-button>
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
    </el-card>

    <!-- 提交诉求 -->
    <el-dialog v-model="submitVisible" title="提交诉求" width="560px">
      <el-form ref="submitRef" :model="submitForm" :rules="submitRules" label-width="80px">
        <el-form-item label="分类" prop="category">
          <el-select v-model="submitForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="submitForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="submitForm.content" type="textarea" :rows="4" placeholder="请详细描述诉求" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="submitForm.location" placeholder="发生地点（可选）" />
        </el-form-item>
        <el-form-item label="匿名">
          <el-switch v-model="submitForm.isAnonymous" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 诉求详情 -->
    <el-dialog v-model="detailVisible" title="诉求详情" width="640px">
      <div v-if="current">
        <h3>{{ current.title }}</h3>
        <div class="meta">
          <el-tag size="small">{{ current.category }}</el-tag>
          <el-tag size="small" :type="statusType(current.status)">{{ statusText(current.status) }}</el-tag>
          <span>{{ current.appealNo }}</span>
        </div>
        <div class="content">{{ current.content }}</div>
        <div v-if="current.processResult" class="result">
          <strong>处理结果：</strong>{{ current.processResult }}
        </div>

        <el-divider>流转记录</el-divider>
        <el-timeline v-if="flows.length">
          <el-timeline-item v-for="f in flows" :key="f.id" :timestamp="f.operateTime">
            {{ operateText(f.operateType) }}：{{ f.operateComment || '—' }}
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无流转记录" :image-size="60" />
      </div>
    </el-dialog>

    <!-- 分派 -->
    <el-dialog v-model="assignVisible" title="分派处理人" width="420px">
      <el-form label-width="80px">
        <el-form-item label="处理人">
          <el-select v-model="assignForm.processorId" placeholder="请选择网格员或村委工作人员" style="width: 100%">
            <el-option v-for="p in processors" :key="p.id" :label="p.realName" :value="p.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="doAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 处理 -->
    <el-dialog v-model="handleVisible" :title="handleTitle" width="520px">
      <el-form label-width="80px">
        <el-form-item label="处理结果">
          <el-input v-model="handleForm.processResult" type="textarea" :rows="4" placeholder="请填写处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="doHandle">提交</el-button>
      </template>
    </el-dialog>

    <!-- 上报村委 -->
    <el-dialog v-model="reportVisible" title="上报村委" width="520px">
      <el-form label-width="80px">
        <el-form-item label="上报说明">
          <el-input v-model="reportForm.comment" type="textarea" :rows="4" placeholder="请说明需要村委协调处理的事项（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="doReport">上报</el-button>
      </template>
    </el-dialog>

    <!-- 催办 -->
    <el-dialog v-model="urgeVisible" title="催办诉求" width="520px">
      <el-form label-width="80px">
        <el-form-item label="催办说明">
          <el-input v-model="urgeForm.comment" type="textarea" :rows="4" placeholder="请填写催办说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="urgeVisible = false">取消</el-button>
        <el-button type="warning" @click="doUrge">催办</el-button>
      </template>
    </el-dialog>

    <!-- 审核退回 -->
    <el-dialog v-model="auditReturnVisible" title="退回重办" width="520px">
      <el-form ref="auditReturnRef" :model="auditReturnForm" :rules="auditReturnRules" label-width="80px">
        <el-form-item label="退回理由" prop="reason">
          <el-input v-model="auditReturnForm.reason" type="textarea" :rows="4" placeholder="请填写退回理由（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditReturnVisible = false">取消</el-button>
        <el-button type="danger" @click="doAuditReturn">退回</el-button>
      </template>
    </el-dialog>

    <!-- 修改处理结果 -->
    <el-dialog v-model="resultVisible" title="修改处理结果" width="520px">
      <el-form ref="resultRef" :model="resultForm" :rules="resultRules" label-width="80px">
        <el-form-item label="处理结果" prop="processResult">
          <el-input v-model="resultForm.processResult" type="textarea" :rows="4" placeholder="请填写处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultVisible = false">取消</el-button>
        <el-button type="primary" @click="doUpdateResult">保存</el-button>
      </template>
    </el-dialog>

    <!-- 不满意 -->
    <el-dialog v-model="dissatisfiedVisible" title="不满意反馈" width="520px">
      <el-form ref="dissatisfiedRef" :model="dissatisfiedForm" :rules="dissatisfiedRules" label-width="90px">
        <el-form-item label="不满意理由" prop="reason">
          <el-input v-model="dissatisfiedForm.reason" type="textarea" :rows="4" placeholder="请填写不满意理由（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dissatisfiedVisible = false">取消</el-button>
        <el-button type="danger" @click="doDissatisfied">提交</el-button>
      </template>
    </el-dialog>

    <!-- 追问 -->
    <el-dialog v-model="followUpVisible" title="追问问题" width="520px">
      <el-form ref="followUpRef" :model="followUpForm" :rules="followUpRules" label-width="90px">
        <el-form-item label="追问内容" prop="question">
          <el-input v-model="followUpForm.question" type="textarea" :rows="4" placeholder="请填写需要追问的问题（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followUpVisible = false">取消</el-button>
        <el-button type="primary" @click="doFollowUp">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  getAppealList, getAppealFlows, submitAppeal, acceptAppeal,
  assignAppeal, handleAppeal, reportAppeal,
  approveAppeal, rejectAppeal, updateAppealResult, urgeAppeal,
  satisfiedAppeal, dissatisfiedAppeal, followUpAppeal
} from '@/api/appeal'
import { getDictByType } from '@/api/dict'
import { getAssignableUsers } from '@/api/user'

const userStore = useUserStore()
const isGridWorker = computed(() => userStore.roles.includes('GRID_WORKER'))
const isVillager = computed(() => userStore.roles.includes('VILLAGER'))
const isAdmin = computed(() => userStore.roles.includes('ADMIN'))
const isCommittee = computed(() =>
  userStore.roles.some((r) => r === 'ADMIN' || r === 'COMMITTEE')
)
const list = ref([])
const loading = ref(false)
const total = ref(0)
const current = ref(null)
const flows = ref([])
const categories = ref([])
const processors = ref([])

const statusMap = { 0: '待受理', 1: '已受理', 2: '处理中', 3: '已答复', 4: '已办结', 5: '已归档' }
const operateMap = {
  1: '提交', 2: '受理', 3: '分派', 4: '转办', 5: '走访',
  6: '答复', 7: '办结', 8: '归档', 9: '评价', 10: '上报村委', 11: '退回',
  12: '审核退回', 13: '修改结果', 14: '催办', 15: '满意', 16: '不满意', 17: '追问'
}
const satisfactionMap = { 1: '满意', 2: '基本满意', 3: '不满意' }
const statusType = (s) => (s === 4 ? 'success' : s === 2 ? 'warning' : s === 0 ? 'info' : 'primary')
const statusText = (s) => {
  // 对网格员而言，待受理/已受理都属于“待处理”
  if (isGridWorker.value && (s === 0 || s === 1)) return '待处理'
  return statusMap[s] || '未知'
}
const operateText = (t) => operateMap[t] || '操作'
const satisfactionText = (s) => satisfactionMap[s] || '未知'
const can = (perm) => userStore.permissions.includes(perm)

// 网格员：待处理(0/1) -> 直接处理/上报村委；处理中(2) -> 填写处理结果/上报村委
// 村委：已受理(1) -> 分派/直接处理；已答复(3) -> 审核通过/退回/修改处理结果
const isPending = (s) => s === 0 || s === 1
const canDirectHandle = (row) =>
  can('appeal:handle') &&
  (isGridWorker.value ? isPending(row.status) : row.status === 1)
const canUrge = (row) =>
  !isAdmin.value && userStore.roles.includes('COMMITTEE') && can('appeal:handle') && row.status === 2
const canFillResult = (row) =>
  can('appeal:handle') && isGridWorker.value && row.status === 2
const canReport = (row) =>
  can('appeal:handle') && isGridWorker.value && (isPending(row.status) || row.status === 2)
// 仅审核网格员处理（有处理人）的已答复诉求；村委直接处理的已办结，只保留详情
const canAudit = (row) =>
  isCommittee.value && can('appeal:finish') && row.status === 3 && row.processorId != null
const canEditResult = (row) =>
  isCommittee.value && can('appeal:handle') && row.status === 3 && row.processorId != null
const canFeedback = (row) =>
  isVillager.value && row.status === 4 && row.satisfaction !== 1
const handleTitle = computed(() =>
  isGridWorker.value && current.value?.status === 2 ? '填写处理结果' : '直接处理诉求'
)

const query = reactive({ pageNum: 1, pageSize: 10, category: '', keyword: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAppealList(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadData()
}

const handleSizeChange = () => {
  query.pageNum = 1
  loadData()
}

const loadCategories = async () => {
  const res = await getDictByType('appeal_category')
  categories.value = (res.data || []).map((d) => d.dictLabel)
}

const loadProcessors = async () => {
  const res = await getAssignableUsers()
  processors.value = res.data || []
}

// 提交
const submitVisible = ref(false)
const submitting = ref(false)
const submitRef = ref()
const submitForm = reactive({ category: '', title: '', content: '', location: '', isAnonymous: 0 })
const submitRules = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}
const openSubmit = () => {
  Object.assign(submitForm, { category: '', title: '', content: '', location: '', isAnonymous: 0 })
  submitVisible.value = true
}
const doSubmit = async () => {
  await submitRef.value.validate()
  submitting.value = true
  try {
    await submitAppeal(submitForm)
    ElMessage.success('提交成功')
    submitVisible.value = false
    handleSearch()
  } finally {
    submitting.value = false
  }
}

// 详情
const detailVisible = ref(false)
const openDetail = async (row) => {
  current.value = row
  detailVisible.value = true
  const res = await getAppealFlows(row.id)
  flows.value = res.data || []
}

// 受理
const doAccept = async (row) => {
  await acceptAppeal(row.id)
  ElMessage.success('已受理')
  loadData()
}

// 分派
const assignVisible = ref(false)
const assignForm = reactive({ processorId: null })
const openAssign = (row) => {
  current.value = row
  assignForm.processorId = null
  assignVisible.value = true
  loadProcessors()
}
const doAssign = async () => {
  await assignAppeal(current.value.id, assignForm)
  ElMessage.success('已分派')
  assignVisible.value = false
  loadData()
}

// 处理
const handleVisible = ref(false)
const handleForm = reactive({ processResult: '' })
const openHandle = (row) => {
  current.value = row
  handleForm.processResult = ''
  handleVisible.value = true
}
const doHandle = async () => {
  if (!handleForm.processResult) return ElMessage.warning('请填写处理结果')
  await handleAppeal(current.value.id, handleForm)
  ElMessage.success('已提交处理结果')
  handleVisible.value = false
  loadData()
}

// 上报村委
const reportVisible = ref(false)
const reportForm = reactive({ comment: '' })
const openReport = (row) => {
  current.value = row
  reportForm.comment = ''
  reportVisible.value = true
}
const doReport = async () => {
  await reportAppeal(current.value.id, reportForm)
  ElMessage.success('已上报村委')
  reportVisible.value = false
  loadData()
}

// 催办
const urgeVisible = ref(false)
const urgeForm = reactive({ comment: '' })
const openUrge = (row) => {
  current.value = row
  urgeForm.comment = ''
  urgeVisible.value = true
}
const doUrge = async () => {
  await urgeAppeal(current.value.id, urgeForm)
  ElMessage.success('已催办')
  urgeVisible.value = false
  loadData()
}

// 村委审核通过
const doApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认审核通过该诉求吗？通过后将变为已办结。', '审核通过', {
      type: 'warning',
      confirmButtonText: '审核通过',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await approveAppeal(row.id)
  ElMessage.success('审核通过')
  loadData()
}

// 村委退回重办
const auditReturnVisible = ref(false)
const auditReturnRef = ref()
const auditReturnForm = reactive({ reason: '' })
const auditReturnRules = {
  reason: [{ required: true, message: '请填写退回理由', trigger: 'blur' }]
}
const openAuditReturn = (row) => {
  current.value = row
  auditReturnForm.reason = ''
  auditReturnVisible.value = true
}
const doAuditReturn = async () => {
  await auditReturnRef.value.validate()
  await rejectAppeal(current.value.id, auditReturnForm)
  ElMessage.success('已退回重办')
  auditReturnVisible.value = false
  loadData()
}

// 村委修改处理结果
const resultVisible = ref(false)
const resultRef = ref()
const resultForm = reactive({ processResult: '' })
const resultRules = {
  processResult: [{ required: true, message: '请填写处理结果', trigger: 'blur' }]
}
const openEditResult = (row) => {
  current.value = row
  resultForm.processResult = row.processResult || ''
  resultVisible.value = true
}
const doUpdateResult = async () => {
  await resultRef.value.validate()
  await updateAppealResult(current.value.id, resultForm)
  ElMessage.success('处理结果已修改')
  resultVisible.value = false
  loadData()
}

// 村民反馈：满意
const doSatisfied = async (row) => {
  try {
    await ElMessageBox.confirm('确认对处理结果表示满意吗？', '满意反馈', {
      type: 'success',
      confirmButtonText: '满意',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await satisfiedAppeal(row.id)
  ElMessage.success('已反馈满意')
  loadData()
}

// 村民反馈：不满意
const dissatisfiedVisible = ref(false)
const dissatisfiedRef = ref()
const dissatisfiedForm = reactive({ reason: '' })
const dissatisfiedRules = {
  reason: [{ required: true, message: '请填写不满意理由', trigger: 'blur' }]
}
const openDissatisfied = (row) => {
  current.value = row
  dissatisfiedForm.reason = ''
  dissatisfiedVisible.value = true
}
const doDissatisfied = async () => {
  await dissatisfiedRef.value.validate()
  await dissatisfiedAppeal(current.value.id, dissatisfiedForm)
  ElMessage.success('已提交不满意反馈，诉求将重新处理')
  dissatisfiedVisible.value = false
  loadData()
}

// 村民反馈：追问
const followUpVisible = ref(false)
const followUpRef = ref()
const followUpForm = reactive({ question: '' })
const followUpRules = {
  question: [{ required: true, message: '请填写追问问题', trigger: 'blur' }]
}
const openFollowUp = (row) => {
  current.value = row
  followUpForm.question = ''
  followUpVisible.value = true
}
const doFollowUp = async () => {
  await followUpRef.value.validate()
  await followUpAppeal(current.value.id, followUpForm)
  ElMessage.success('已提交追问，诉求将重新办理')
  followUpVisible.value = false
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
  gap: 10px;
  align-items: center;
  margin: 12px 0;
}

.content {
  padding: 14px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.result {
  margin-top: 12px;
  padding: 12px;
  background: #f0f9eb;
  border-radius: 6px;
  color: #67c23a;
  line-height: 1.8;
}
</style>
