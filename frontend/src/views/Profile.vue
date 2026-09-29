<template>
  <div class="profile">
    <div class="toolbar">
      <h3>个人资料</h3>
    </div>

    <el-row :gutter="16">
      <!-- 左：头像卡 -->
      <el-col :xs="24" :sm="8">
        <el-card class="side-card" shadow="never">
          <div class="avatar-wrap">
            <el-avatar :size="80" :src="form.avatar">{{ avatarFallback }}</el-avatar>
            <div class="nick">{{ form.nickname || auth.user?.nickname || auth.user?.username }}</div>
            <div class="uname">@{{ auth.user?.username }}</div>
          </div>
          <el-divider />
          <div class="meta">
            <span class="label">加入时间</span>
            <span>{{ joinedAt }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右：可编辑表单 -->
      <el-col :xs="24" :sm="16">
        <el-card shadow="never">
          <el-form :model="form" label-width="80px">
            <el-form-item label="用户名">
              <el-input :model-value="auth.user?.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input :model-value="auth.user?.email" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="头像 URL">
              <el-input v-model="form.avatar" placeholder="头像图片链接（可选）" />
            </el-form-item>
            <div class="actions">
              <el-button type="primary" :loading="saving" @click="save">保存</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as userApi from '../api/user'

const auth = useAuthStore()
const saving = ref(false)
const form = reactive({ nickname: '', avatar: '' })

// 头像回退字母：无头像链接时显示昵称/用户名首字符
const avatarFallback = computed(() =>
  (form.nickname || auth.user?.nickname || auth.user?.username || '?').charAt(0),
)

// 加入时间：createdAt 是 ISO 字符串，只取日期部分
const joinedAt = computed(() => auth.user?.createdAt?.slice(0, 10) || '—')

onMounted(() => {
  form.nickname = auth.user?.nickname || ''
  form.avatar = auth.user?.avatar || ''
})

async function save() {
  saving.value = true
  try {
    await userApi.updateProfile({ nickname: form.nickname, avatar: form.avatar })
    await auth.fetchMe()
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile {
  max-width: 760px;
  margin: 0 auto;
}
.toolbar {
  margin-bottom: 16px;
}
.toolbar h3 {
  margin: 0;
}
.side-card {
  text-align: center;
}
.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.nick {
  font-size: 16px;
  font-weight: 600;
}
.uname {
  color: #909399;
  font-size: 13px;
}
.meta {
  display: flex;
  justify-content: space-between;
  color: #606266;
  font-size: 13px;
}
.meta .label {
  color: #909399;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
