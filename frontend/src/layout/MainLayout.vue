<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">TaskFlow</div>
      <el-menu :default-active="$route.path" router class="menu">
        <el-menu-item index="/teams">
          <el-icon><UserFilled /></el-icon>
          <span>我的团队</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>个人资料</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="title">团队任务协作平台</span>

        <div class="header-right">
          <!-- 通知铃铛 -->
          <el-popover placement="bottom-end" :width="360" trigger="click" @show="loadNotifications">
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="bell-badge">
                <el-icon class="bell" :size="20"><Bell /></el-icon>
              </el-badge>
            </template>

            <div class="notif-panel">
              <div class="notif-head">
                <span>通知</span>
                <el-button v-if="unreadCount > 0" link type="primary" size="small" @click="handleMarkAll">
                  全部已读
                </el-button>
              </div>
              <div v-if="notifications.length === 0" class="notif-empty">暂无通知</div>
              <div v-else class="notif-list">
                <div
                  v-for="n in notifications"
                  :key="n.id"
                  class="notif-item"
                  :class="{ unread: n.isRead === 0 }"
                  @click="handleClick(n)"
                >
                  <div class="notif-title">
                    <el-tag size="small" type="info" effect="plain">{{ NOTIFICATION_TYPE_LABELS[n.type] || n.type }}</el-tag>
                  </div>
                  <div class="notif-content">{{ n.content }}</div>
                  <div class="notif-time">{{ formatTime(n.createdAt) }}</div>
                </div>
              </div>
            </div>
          </el-popover>

          <!-- 用户下拉 -->
          <el-dropdown @command="handleCommand">
            <span class="user">
              {{ auth.user?.nickname || auth.user?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { listNotifications, getUnreadCount, markRead, markAllRead } from '../api/notification'
import { NOTIFICATION_TYPE_LABELS } from '../utils/dict'

const router = useRouter()
const auth = useAuthStore()

const unreadCount = ref(0)
const notifications = ref([])

// 未读数（顶栏红点）
async function loadUnreadCount() {
  unreadCount.value = await getUnreadCount()
}

// 通知列表（弹层打开时拉取第一页，最多 20 条）
async function loadNotifications() {
  const page = await listNotifications(1, 20)
  notifications.value = page.records
}

// 全部已读
async function handleMarkAll() {
  await markAllRead()
  unreadCount.value = 0
  notifications.value = notifications.value.map((n) => ({ ...n, isRead: 1 }))
}

// 点单条：标已读 + 跳对应页面
function handleClick(n) {
  if (n.isRead === 0) {
    markRead(n.id).then(() => {
      n.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    })
  }
  router.push(notifyTarget(n))
}

// 通知 -> 跳转路径（type 决定 relatedId 含义）
function notifyTarget(n) {
  if (n.type.startsWith('TASK_')) return `/tasks/${n.relatedId}`
  if (n.type === 'TEAM_INVITED') return `/teams/${n.relatedId}`
  if (n.type === 'PROJECT_INVITED') return `/projects/${n.relatedId}`
  return '/teams'
}

function formatTime(s) {
  if (!s) return ''
  return s.replace('T', ' ').slice(0, 16)
}

async function handleCommand(command) {
  if (command === 'logout') {
    await auth.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(loadUnreadCount)
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #001529;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}
.menu {
  border-right: none;
  background: #001529;
  --el-menu-text-color: rgba(255, 255, 255, 0.65);
  --el-menu-active-color: #fff;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  background: #fff;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.bell-badge {
  cursor: pointer;
}
.bell {
  color: #555;
}
.user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.main {
  background: #f5f7fa;
}
.notif-panel {
  display: flex;
  flex-direction: column;
}
.notif-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 8px;
}
.notif-empty {
  color: #999;
  text-align: center;
  padding: 24px 0;
}
.notif-list {
  max-height: 360px;
  overflow-y: auto;
}
.notif-item {
  padding: 10px 8px;
  border-radius: 6px;
  cursor: pointer;
}
.notif-item:hover {
  background: #f5f7fa;
}
.notif-item.unread {
  background: #ecf5ff;
}
.notif-title {
  margin-bottom: 2px;
}
.notif-content {
  font-size: 13px;
  color: #333;
  line-height: 1.4;
}
.notif-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
