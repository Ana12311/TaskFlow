<template>
  <div>
    <div class="toolbar">
      <h3>我的团队</h3>
      <el-button type="primary" :icon="Plus" @click="dialogVisible = true">新建团队</el-button>
    </div>

    <el-empty v-if="!loading && teams.length === 0" description="还没有团队，点右上角创建一个" />

    <el-row :gutter="16">
      <el-col v-for="team in teams" :key="team.id" :span="8">
        <el-card class="team-card" shadow="hover" @click="router.push(`/teams/${team.id}`)">
          <div class="team-name">{{ team.name }}</div>
          <div class="team-desc">{{ team.description || '暂无描述' }}</div>
          <div class="team-meta">
            <el-tag size="small">{{ roleLabel(team.myRole) }}</el-tag>
            <span class="count">{{ team.memberCount }} 人</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" title="新建团队" width="440px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="团队名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="团队描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as teamApi from '../api/team'

const router = useRouter()
const teams = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const creating = ref(false)
const form = reactive({ name: '', description: '' })

const ROLE_LABELS = { OWNER: '拥有者', ADMIN: '管理员', MEMBER: '成员' }
const roleLabel = (role) => ROLE_LABELS[role] || role

async function load() {
  loading.value = true
  try {
    teams.value = await teamApi.listTeams()
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!form.name) {
    ElMessage.warning('请输入团队名称')
    return
  }
  creating.value = true
  try {
    await teamApi.createTeam({ name: form.name, description: form.description })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    form.name = ''
    form.description = ''
    await load()
  } finally {
    creating.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.team-card {
  cursor: pointer;
  margin-bottom: 16px;
}
.team-name {
  font-size: 16px;
  font-weight: 600;
}
.team-desc {
  color: #909399;
  font-size: 13px;
  margin: 8px 0 12px;
  min-height: 20px;
}
.team-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.count {
  color: #909399;
  font-size: 12px;
}
</style>
