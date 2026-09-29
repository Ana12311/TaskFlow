<template>
  <div>
    <el-page-header class="back" @back="router.push(`/teams/${project.teamId}`)">
      <template #content>
        <span class="name">{{ project.name }}</span>
        <span class="desc">{{ project.description }}</span>
      </template>
      <template #extra>
        <el-button type="danger" plain :icon="Delete" @click="removeProject">删除项目</el-button>
      </template>
    </el-page-header>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats">
      <el-col :span="4"><div class="stat total">{{ stats.total }}<div class="label">全部</div></div></el-col>
      <el-col :span="4"><div class="stat todo">{{ stats.todo }}<div class="label">待办</div></div></el-col>
      <el-col :span="4"><div class="stat progress">{{ stats.inProgress }}<div class="label">进行中</div></div></el-col>
      <el-col :span="4"><div class="stat done">{{ stats.done }}<div class="label">已完成</div></div></el-col>
      <el-col :span="4"><div class="stat cancelled">{{ stats.cancelled }}<div class="label">已取消</div></div></el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="任务" name="tasks">
        <div class="toolbar">
          <span></span>
          <el-button type="primary" :icon="Plus" @click="taskDialog = true">新建任务</el-button>
        </div>
        <el-table :data="tasks" stripe>
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="assigneeName" label="负责人" width="120">
            <template #default="{ row }">{{ row.assigneeName || '未分配' }}</template>
          </el-table-column>
          <el-table-column label="优先级" width="100">
            <template #default="{ row }">
              <el-tag :type="PRIORITY_MAP[row.priority]?.type" size="small">{{ PRIORITY_MAP[row.priority]?.label || row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="STATUS_MAP[row.status]?.type" size="small">{{ STATUS_MAP[row.status]?.label || row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="primary" @click="router.push(`/tasks/${row.id}`)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="taskTotal > taskPageSize"
          class="pager"
          background
          layout="total, prev, pager, next"
          :total="taskTotal"
          :page-size="taskPageSize"
          v-model:current-page="taskPage"
          @current-change="loadTasks"
        />
      </el-tab-pane>

      <el-tab-pane label="成员" name="members">
        <div class="toolbar">
          <span></span>
          <el-button type="primary" :icon="Plus" @click="memberDialog = true">添加成员</el-button>
        </div>
        <el-table :data="project.members || []" stripe>
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-tag :type="row.role === 'OWNER' ? 'danger' : ''" size="small">{{ ROLE_LABELS[row.role] || row.role }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="row.role !== 'OWNER'" link type="danger" @click="removeMember(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建任务 -->
    <el-dialog v-model="taskDialog" title="新建任务" width="480px">
      <el-form :model="taskForm" label-width="60px">
        <el-form-item label="标题" required>
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="taskForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority" style="width: 100%">
            <el-option v-for="(v, k) in PRIORITY_MAP" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="taskForm.assigneeId" clearable placeholder="可选" style="width: 100%">
            <el-option v-for="m in project.members || []" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialog = false">取消</el-button>
        <el-button type="primary" @click="createTask">创建</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员 -->
    <el-dialog v-model="memberDialog" title="添加成员" width="440px">
      <el-form :model="memberForm" label-width="80px">
        <el-form-item label="用户 ID" required>
          <el-input v-model="memberForm.userId" placeholder="输入用户 ID" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="memberForm.role" style="width: 100%">
            <el-option label="成员" value="MEMBER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialog = false">取消</el-button>
        <el-button type="primary" @click="addMember">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as projectApi from '../api/project'
import * as taskApi from '../api/task'
import { STATUS_MAP, PRIORITY_MAP, ROLE_LABELS } from '../utils/dict'

const route = useRoute()
const router = useRouter()
const projectId = route.params.projectId

const project = ref({ members: [] })
const tasks = ref([])
const taskTotal = ref(0)
const taskPage = ref(1)
const taskPageSize = ref(10)
const stats = ref({ total: 0, todo: 0, inProgress: 0, done: 0, cancelled: 0 })
const activeTab = ref('tasks')

const taskDialog = ref(false)
const taskForm = reactive({ title: '', description: '', priority: 'MEDIUM', assigneeId: null })

const memberDialog = ref(false)
const memberForm = reactive({ userId: '', role: 'MEMBER' })

async function load() {
  project.value = await projectApi.getProject(projectId)
  await loadTasks()
  stats.value = await taskApi.getStats(projectId)
}

async function loadTasks() {
  const page = await taskApi.listTasks(projectId, taskPage.value, taskPageSize.value)
  tasks.value = page.records
  taskTotal.value = page.total
}

async function createTask() {
  if (!taskForm.title) {
    ElMessage.warning('请输入标题')
    return
  }
  await taskApi.createTask(projectId, {
    title: taskForm.title,
    description: taskForm.description,
    priority: taskForm.priority,
    assigneeId: taskForm.assigneeId || null,
  })
  ElMessage.success('创建成功')
  taskDialog.value = false
  taskForm.title = ''
  taskForm.description = ''
  taskForm.priority = 'MEDIUM'
  taskForm.assigneeId = null
  await load()
}

async function addMember() {
  if (!memberForm.userId) {
    ElMessage.warning('请输入用户 ID')
    return
  }
  await projectApi.addMember(projectId, { userId: Number(memberForm.userId), role: memberForm.role })
  ElMessage.success('添加成功')
  memberDialog.value = false
  memberForm.userId = ''
  await load()
}

async function removeMember(row) {
  await ElMessageBox.confirm(`确定移除成员 ${row.nickname || row.username} 吗？`, '提示', { type: 'warning' })
  await projectApi.removeMember(projectId, row.userId)
  ElMessage.success('已移除')
  await load()
}

async function removeProject() {
  await ElMessageBox.confirm('删除项目会级联删除所有任务、评论、日志，且不可恢复，确定？', '危险操作', { type: 'warning' })
  await projectApi.deleteProject(projectId)
  ElMessage.success('已删除')
  router.push(`/teams/${project.value.teamId}`)
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
  margin-right: 12px;
}
.desc {
  color: #909399;
  font-size: 13px;
}
.stats {
  margin-bottom: 16px;
}
.stat {
  text-align: center;
  background: #fff;
  border-radius: 8px;
  padding: 16px 0;
  font-size: 28px;
  font-weight: bold;
  border: 1px solid #ebeef5;
}
.stat .label {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
  margin-top: 4px;
}
.stat.total { color: #409eff; }
.stat.todo { color: #909399; }
.stat.progress { color: #e6a23c; }
.stat.done { color: #67c23a; }
.stat.cancelled { color: #f56c6c; }
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
