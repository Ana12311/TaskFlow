<template>
  <div>
    <el-page-header class="back" @back="router.push(`/projects/${task.projectId}`)">
      <template #content>
        <span class="name">{{ task.title }}</span>
      </template>
    </el-page-header>

    <el-row :gutter="16">
      <el-col :span="16">
        <!-- 任务信息 -->
        <el-card class="block">
          <div class="desc">{{ task.description || '暂无描述' }}</div>
          <el-descriptions :column="2" border class="meta">
            <el-descriptions-item label="创建人">{{ task.creatorName }}</el-descriptions-item>
            <el-descriptions-item label="负责人">
              <el-select v-model="task.assigneeId" size="small" clearable placeholder="未分配" @change="changeAssignee">
                <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
              </el-select>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-select v-model="task.priority" size="small" @change="(v) => changePriority(v)">
                <el-option v-for="(val, k) in PRIORITY_MAP" :key="k" :label="val.label" :value="k" />
              </el-select>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-select v-model="task.status" size="small" @change="(v) => changeStatus(v)">
                <el-option v-for="(val, k) in STATUS_MAP" :key="k" :label="val.label" :value="k" />
              </el-select>
            </el-descriptions-item>
            <el-descriptions-item label="截止日期">{{ task.dueDate || '无' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(task.createdAt) }}</el-descriptions-item>
          </el-descriptions>

          <div class="actions">
            <el-button type="primary" :icon="Edit" @click="openEdit">编辑</el-button>
            <el-button type="danger" :icon="Delete" @click="remove">删除</el-button>
          </div>
        </el-card>

        <!-- 评论 -->
        <el-card class="block">
          <template #header>评论（{{ commentTotal }}）</template>
          <div class="comment-input">
            <el-input v-model="commentText" type="textarea" :rows="2" placeholder="说点什么..." />
            <el-button type="primary" class="comment-btn" @click="addComment">发表</el-button>
          </div>
          <el-empty v-if="comments.length === 0" description="还没有评论" />
          <div v-for="c in comments" :key="c.id" class="comment">
            <div class="comment-head">
              <span class="comment-user">{{ c.nickname }}</span>
              <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
              <el-button v-if="c.userId === auth.user?.id" link type="danger" size="small" @click="removeComment(c)">删除</el-button>
            </div>
            <div class="comment-content">{{ c.content }}</div>
          </div>
          <el-pagination
            v-if="commentTotal > commentPageSize"
            class="pager"
            background
            layout="total, prev, pager, next"
            :total="commentTotal"
            :page-size="commentPageSize"
            v-model:current-page="commentPage"
            @current-change="loadComments"
          />
        </el-card>
      </el-col>

      <!-- 操作日志 -->
      <el-col :span="8">
        <el-card class="block">
          <template #header>操作日志</template>
          <el-empty v-if="logs.length === 0" description="暂无日志" />
          <el-timeline>
            <el-timeline-item v-for="log in logs" :key="log.id" :timestamp="formatTime(log.createdAt)">
              <div>{{ OPERATION_LABELS[log.operationType] || log.operationType }}</div>
              <div v-if="log.beforeValue || log.afterValue" class="log-value">
                {{ log.beforeValue || '空' }} → {{ log.afterValue || '空' }}
              </div>
              <div class="log-user">{{ log.operatorName }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-pagination
            v-if="logTotal > logPageSize"
            class="pager"
            background
            layout="total, prev, pager, next"
            :total="logTotal"
            :page-size="logPageSize"
            v-model:current-page="logPage"
            @current-change="loadLogs"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑任务 -->
    <el-dialog v-model="editDialog" title="编辑任务" width="480px">
      <el-form :model="editForm" label-width="60px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="截止">
          <el-date-picker v-model="editForm.dueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as taskApi from '../api/task'
import * as commentApi from '../api/comment'
import * as projectApi from '../api/project'
import { useAuthStore } from '../stores/auth'
import { STATUS_MAP, PRIORITY_MAP, OPERATION_LABELS } from '../utils/dict'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const taskId = route.params.taskId

const task = ref({})
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(10)
const logs = ref([])
const logTotal = ref(0)
const logPage = ref(1)
const logPageSize = ref(10)
const members = ref([])
const commentText = ref('')
const editDialog = ref(false)
const editForm = reactive({ title: '', description: '', dueDate: null })

async function load() {
  task.value = await taskApi.getTask(taskId)
  await loadComments()
  await loadLogs()
  if (task.value.projectId) {
    const project = await projectApi.getProject(task.value.projectId)
    members.value = project.members || []
  }
}

async function loadComments() {
  const page = await commentApi.listComments(taskId, commentPage.value, commentPageSize.value)
  comments.value = page.records
  commentTotal.value = page.total
}

async function loadLogs() {
  const page = await taskApi.listLogs(taskId, logPage.value, logPageSize.value)
  logs.value = page.records
  logTotal.value = page.total
}

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

async function changeStatus(status) {
  await taskApi.updateStatus(taskId, status)
  ElMessage.success('状态已更新')
  await load()
}

async function changePriority(priority) {
  await taskApi.updatePriority(taskId, priority)
  ElMessage.success('优先级已更新')
  await load()
}

async function changeAssignee(uid) {
  await taskApi.assignTask(taskId, uid || null)
  ElMessage.success('负责人已更新')
  await load()
}

function openEdit() {
  editForm.title = task.value.title
  editForm.description = task.value.description
  editForm.dueDate = task.value.dueDate
  editDialog.value = true
}

async function saveEdit() {
  await taskApi.updateTask(taskId, {
    title: editForm.title,
    description: editForm.description,
    dueDate: editForm.dueDate,
  })
  ElMessage.success('已保存')
  editDialog.value = false
  await load()
}

async function remove() {
  await ElMessageBox.confirm('确定删除该任务吗？', '提示', { type: 'warning' })
  await taskApi.deleteTask(taskId)
  ElMessage.success('已删除')
  router.push(`/projects/${task.value.projectId}`)
}

async function addComment() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await commentApi.createComment(taskId, commentText.value)
  commentText.value = ''
  await load()
}

async function removeComment(c) {
  await commentApi.deleteComment(c.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.back {
  margin-bottom: 16px;
}
.name {
  font-size: 18px;
  font-weight: 600;
}
.block {
  margin-bottom: 16px;
}
.desc {
  color: #606266;
  margin-bottom: 12px;
}
.meta {
  margin-bottom: 12px;
}
.actions {
  display: flex;
  gap: 8px;
}
.comment-input {
  margin-bottom: 16px;
}
.comment-btn {
  margin-top: 8px;
}
.comment {
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 0;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-user {
  font-weight: 600;
}
.comment-time {
  color: #909399;
  font-size: 12px;
  flex: 1;
}
.comment-content {
  margin-top: 4px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.log-value {
  color: #909399;
  font-size: 12px;
}
.log-user {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
