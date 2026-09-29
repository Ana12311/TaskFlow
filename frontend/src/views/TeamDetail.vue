<template>
  <div>
    <el-page-header class="back" @back="router.push('/teams')">
      <template #content>
        <span class="name">{{ team.name }}</span>
        <span class="desc">{{ team.description }}</span>
      </template>
    </el-page-header>

    <el-tabs v-model="activeTab">
      <!-- 项目列表 -->
      <el-tab-pane label="项目" name="projects">
        <div class="toolbar">
          <span></span>
          <el-button type="primary" :icon="Plus" @click="projectDialog = true">新建项目</el-button>
        </div>
        <el-empty v-if="projects.length === 0" description="还没有项目" />
        <el-row :gutter="16">
          <el-col v-for="p in projects" :key="p.id" :span="8">
            <el-card class="project-card" shadow="hover" @click="router.push(`/projects/${p.id}`)">
              <div class="p-name">{{ p.name }}</div>
              <div class="p-desc">{{ p.description || '暂无描述' }}</div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 成员管理 -->
      <el-tab-pane label="成员" name="members">
        <div class="toolbar">
          <span></span>
          <el-button type="primary" :icon="Plus" @click="memberDialog = true">添加成员</el-button>
        </div>
        <el-table :data="team.members || []" stripe>
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column label="角色" width="200">
            <template #default="{ row }">
              <el-select v-model="row.role" size="small" :disabled="row.role === 'OWNER'" @change="(v) => changeRole(row, v)">
                <el-option label="拥有者" value="OWNER" />
                <el-option label="管理员" value="ADMIN" />
                <el-option label="成员" value="MEMBER" />
              </el-select>
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

    <!-- 新建项目 -->
    <el-dialog v-model="projectDialog" title="新建项目" width="440px">
      <el-form :model="projectForm" label-width="60px">
        <el-form-item label="名称" required>
          <el-input v-model="projectForm.name" placeholder="项目名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="projectForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="projectDialog = false">取消</el-button>
        <el-button type="primary" @click="createProject">创建</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员 -->
    <el-dialog v-model="memberDialog" title="添加成员" width="440px">
      <el-form :model="memberForm" label-width="80px">
        <el-form-item label="用户 ID" required>
          <el-input v-model="memberForm.userId" placeholder="输入要添加的用户 ID" />
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as teamApi from '../api/team'
import * as projectApi from '../api/project'

const route = useRoute()
const router = useRouter()
const teamId = route.params.teamId

const team = ref({ members: [] })
const projects = ref([])
const activeTab = ref('projects')

const projectDialog = ref(false)
const projectForm = reactive({ name: '', description: '' })

const memberDialog = ref(false)
const memberForm = reactive({ userId: '', role: 'MEMBER' })

async function load() {
  team.value = await teamApi.getTeam(teamId)
  projects.value = await projectApi.listTeamProjects(teamId)
}

async function createProject() {
  if (!projectForm.name) {
    ElMessage.warning('请输入项目名称')
    return
  }
  await projectApi.createProject(teamId, { name: projectForm.name, description: projectForm.description })
  ElMessage.success('创建成功')
  projectDialog.value = false
  projectForm.name = ''
  projectForm.description = ''
  await load()
}

async function addMember() {
  if (!memberForm.userId) {
    ElMessage.warning('请输入用户 ID')
    return
  }
  await teamApi.addMember(teamId, { userId: Number(memberForm.userId), role: memberForm.role })
  ElMessage.success('添加成功')
  memberDialog.value = false
  memberForm.userId = ''
  await load()
}

async function removeMember(row) {
  await ElMessageBox.confirm(`确定移除成员 ${row.nickname || row.username} 吗？`, '提示', { type: 'warning' })
  await teamApi.removeMember(teamId, row.userId)
  ElMessage.success('已移除')
  await load()
}

async function changeRole(row, role) {
  await teamApi.updateRole(teamId, row.userId, role)
  ElMessage.success('角色已更新')
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
  margin-right: 12px;
}
.desc {
  color: #909399;
  font-size: 13px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.project-card {
  cursor: pointer;
  margin-bottom: 16px;
}
.p-name {
  font-size: 16px;
  font-weight: 600;
}
.p-desc {
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
}
</style>
