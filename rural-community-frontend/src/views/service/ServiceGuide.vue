<template>
  <el-card>
    <el-tabs v-model="activeTab">
      <!-- 村委：办事审批；网格员：办事初审 -->
      <el-tab-pane v-if="isStaff" :label="isCommittee ? '办事审批' : '办事初审'" name="approve">
        <el-table :data="applies" v-loading="loadingApplies" stripe>
          <el-table-column prop="applyNo" label="编号" width="160" />
          <el-table-column label="事项" min-width="140">
            <template #default="{ row }">{{ itemName(row.itemId) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="applyStatusType(row)">{{ applyStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="170" />
          <el-table-column label="办理结果" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.resultContent || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="210" fixed="right">
            <template #default="{ row }">
              <template v-if="canReview(row)">
                <el-button link type="success" @click="doApprove(row)">审核通过</el-button>
                <el-button link type="danger" @click="openReject(row)">驳回</el-button>
              </template>
              <template v-else-if="canGridAudit(row)">
                <el-button link type="success" @click="doGridPass(row)">初审通过</el-button>
                <el-button link type="danger" @click="openGridReject(row)">初审不通过</el-button>
              </template>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
          v-model:current-page="applyQuery.pageNum"
          v-model:page-size="applyQuery.pageSize"
          :total="applyTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadApplies"
          @size-change="loadApplies"
        />
      </el-tab-pane>

      <!-- 事项指南（村委）/ 办事指南（村民） -->
      <el-tab-pane :label="isStaff ? '事项指南' : '办事指南'" name="guide">
        <el-table :data="items" v-loading="loadingItems" stripe>
          <el-table-column prop="itemName" label="事项名称" min-width="160" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column label="办理时限" width="100">
            <template #default="{ row }">{{ row.timeLimit }} 个工作日</template>
          </el-table-column>
          <el-table-column label="需网格初审" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.needGridAudit === 1" type="warning" size="small">需初审</el-tag>
              <span v-else>否</span>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="办理地点" min-width="140" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openItemDetail(row)">
                {{ isCommittee ? '查看详情' : '查看指南' }}
              </el-button>
              <el-button v-if="!isStaff && can('service:apply')" link type="success" @click="openApply(row)">在线申办</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 村民：我的申办 -->
      <el-tab-pane v-if="!isStaff" label="我的申办" name="apply">
        <el-table :data="applies" v-loading="loadingApplies" stripe>
          <el-table-column prop="applyNo" label="编号" width="160" />
          <el-table-column label="事项" min-width="140">
            <template #default="{ row }">{{ itemName(row.itemId) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="applyStatusType(row)">{{ applyStatusText(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="170" />
          <el-table-column label="办理结果" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.resultContent || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canResubmit(row)" link type="primary" @click="openResubmit(row)">重新提交</el-button>
              <el-tag v-else-if="row.status === 4 && row.resubmitted" type="info" size="small">已重新提交</el-tag>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
          v-model:current-page="applyQuery.pageNum"
          v-model:page-size="applyQuery.pageSize"
          :total="applyTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadApplies"
          @size-change="loadApplies"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 事项详情 -->
    <el-dialog v-model="itemDetailVisible" :title="isCommittee ? '事项详情' : '办事指南'" width="600px">
      <div v-if="currentItem">
        <h3>{{ currentItem.itemName }}</h3>
        <div class="guide-section">
          <h4>办理条件</h4>
          <p>{{ currentItem.conditionDesc || '—' }}</p>
        </div>
        <div class="guide-section">
          <h4>所需材料</h4>
          <p>{{ currentItem.materialDesc || '—' }}</p>
        </div>
        <div class="guide-section">
          <h4>办理流程</h4>
          <p>{{ currentItem.processDesc || '—' }}</p>
        </div>
        <div class="guide-meta">
          <span>办理时限：{{ currentItem.timeLimit }} 个工作日</span>
          <span>办理地点：{{ currentItem.location || '—' }}</span>
          <span>咨询电话：{{ currentItem.contactPhone || '—' }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 在线申办 -->
    <el-dialog v-model="applyVisible" :title="applyMode === 'resubmit' ? '重新提交' : '在线申办'" width="520px">
      <el-form ref="applyRef" :model="applyForm" :rules="applyRules" label-width="90px">
        <el-form-item label="事项">
          <el-input :model-value="currentItem?.itemName" disabled />
        </el-form-item>
        <el-form-item label="申请内容" prop="applyContent">
          <el-input v-model="applyForm.applyContent" type="textarea" :rows="4" placeholder="请填写申请说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 驳回 / 初审不通过 -->
    <el-dialog v-model="rejectVisible" :title="rejectMode === 'grid' ? '初审不通过' : '驳回申请'" width="520px">
      <el-form ref="rejectRef" :model="rejectForm" :rules="rejectRules" label-width="100px">
        <el-form-item :label="rejectMode === 'grid' ? '不通过理由' : '驳回理由'" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            :placeholder="rejectMode === 'grid' ? '请填写初审不通过理由（必填）' : '请填写驳回理由（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="doReject">{{ rejectMode === 'grid' ? '提交' : '驳回' }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  getServiceItems, getServiceApplies, submitServiceApply,
  approveServiceApply, rejectServiceApply,
  gridPassServiceApply, gridRejectServiceApply
} from '@/api/service'

const userStore = useUserStore()
const isCommittee = computed(() =>
  userStore.roles.some((r) => r === 'ADMIN' || r === 'COMMITTEE')
)
const isGridWorker = computed(() => userStore.roles.includes('GRID_WORKER'))
const isStaff = computed(() => isCommittee.value || isGridWorker.value)
const activeTab = ref(isStaff.value ? 'approve' : 'guide')
const items = ref([])
const applies = ref([])
const loadingItems = ref(false)
const loadingApplies = ref(false)
const currentItem = ref(null)
const currentApply = ref(null)
const applyQuery = reactive({ pageNum: 1, pageSize: 10 })
const applyTotal = ref(0)

const applyStatusMap = { 0: '待受理', 1: '已受理', 2: '办理中', 3: '待补正', 4: '已退回', 5: '已办结', 6: '已归档' }
const can = (perm) => userStore.permissions.includes(perm)
const itemName = (id) => items.value.find((i) => i.id === id)?.itemName || id
const itemNeedsGridAudit = (itemId) => items.value.find((i) => i.id === itemId)?.needGridAudit === 1

// 村委可审核：待审核，且无需网格初审或网格初审已通过
const canReview = (row) =>
  isCommittee.value && can('service:audit') && row.status === 0 &&
  (!itemNeedsGridAudit(row.itemId) || row.gridAuditBy != null)

// 网格员可初审：本网格待审核且需网格初审、尚未初审
const canGridAudit = (row) =>
  isGridWorker.value && can('service:audit') && row.status === 0 &&
  row.gridAuditBy == null && itemNeedsGridAudit(row.itemId)

// 村民被驳回的申请可重新提交
const canResubmit = (row) =>
  !isStaff.value && can('service:apply') && row.status === 4 && !row.resubmitted

const applyStatusText = (row) => {
  if (row.status === 0) {
    const needGrid = itemNeedsGridAudit(row.itemId)
    if (isGridWorker.value) {
      return needGrid && row.gridAuditBy == null ? '待初审' : '待村委审核'
    }
    if (isCommittee.value) {
      return needGrid && row.gridAuditBy == null ? '待网格初审' : '待审核'
    }
    return '待受理'
  }
  return applyStatusMap[row.status] || '未知'
}

const applyStatusType = (row) => {
  if (row.status === 0) {
    return itemNeedsGridAudit(row.itemId) && row.gridAuditBy == null ? 'warning' : 'info'
  }
  const s = row.status
  return s === 5 ? 'success' : s === 1 ? 'primary' : s === 4 ? 'danger' : 'warning'
}

const loadItems = async () => {
  loadingItems.value = true
  try {
    const res = await getServiceItems()
    items.value = res.data || []
  } finally {
    loadingItems.value = false
  }
}

const loadApplies = async () => {
  loadingApplies.value = true
  try {
    const res = await getServiceApplies(applyQuery)
    applies.value = res.data.records || []
    applyTotal.value = res.data.total || 0
  } finally {
    loadingApplies.value = false
  }
}

// 事项详情
const itemDetailVisible = ref(false)
const openItemDetail = (row) => {
  currentItem.value = row
  itemDetailVisible.value = true
}

// 在线申办 / 重新提交
const applyVisible = ref(false)
const applyMode = ref('new')
const submitting = ref(false)
const applyRef = ref()
const applyForm = reactive({ applyContent: '' })
const applyRules = {
  applyContent: [{ required: true, message: '请填写申请内容', trigger: 'blur' }]
}
const openApply = (row) => {
  currentItem.value = row
  applyForm.applyContent = ''
  applyMode.value = 'new'
  applyVisible.value = true
}
const openResubmit = (row) => {
  const item = items.value.find((i) => i.id === row.itemId)
  if (!item) return ElMessage.warning('事项不存在，无法重新提交')
  currentItem.value = item
  applyForm.applyContent = row.applyContent || ''
  applyMode.value = 'resubmit'
  applyVisible.value = true
}
const doApply = async () => {
  await applyRef.value.validate()
  submitting.value = true
  try {
    await submitServiceApply({ itemId: currentItem.value.id, applyContent: applyForm.applyContent })
    ElMessage.success(applyMode.value === 'resubmit' ? '重新提交成功' : '申请已提交')
    applyVisible.value = false
    loadApplies()
  } finally {
    submitting.value = false
  }
}

// 审核通过
const doApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认审核通过「${itemName(row.itemId)}」的申请吗？`, '审核通过', {
      type: 'success',
      confirmButtonText: '审核通过',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await approveServiceApply(row.id)
  ElMessage.success('审核通过')
  loadApplies()
}

// 网格员初审通过
const doGridPass = async (row) => {
  try {
    await ElMessageBox.confirm(`确认「${itemName(row.itemId)}」申请初审通过吗？`, '初审通过', {
      type: 'success',
      confirmButtonText: '初审通过',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await gridPassServiceApply(row.id, { comment: '' })
  ElMessage.success('初审通过，已提交村委审核')
  loadApplies()
}

// 驳回 / 初审不通过
const rejectVisible = ref(false)
const rejectMode = ref('committee')
const rejectRef = ref()
const rejectForm = reactive({ reason: '' })
const rejectRules = {
  reason: [{ required: true, message: '请填写理由', trigger: 'blur' }]
}
const openReject = (row) => {
  currentApply.value = row
  rejectMode.value = 'committee'
  rejectForm.reason = ''
  rejectVisible.value = true
}
const openGridReject = (row) => {
  currentApply.value = row
  rejectMode.value = 'grid'
  rejectForm.reason = ''
  rejectVisible.value = true
}
const doReject = async () => {
  await rejectRef.value.validate()
  if (rejectMode.value === 'grid') {
    await gridRejectServiceApply(currentApply.value.id, rejectForm)
    ElMessage.success('初审不通过，已退回村民')
  } else {
    await rejectServiceApply(currentApply.value.id, rejectForm)
    ElMessage.success('已驳回')
  }
  rejectVisible.value = false
  loadApplies()
}

onMounted(() => {
  loadItems()
  loadApplies()
})
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.guide-section {
  margin: 16px 0;
}

.guide-section h4 {
  margin-bottom: 6px;
  color: #303133;
}

.guide-section p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.guide-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 13px;
}
</style>
