<template>
    <div class="layout">
        <header class="header">
            <strong>客服工单系统</strong>
            <div class="user">
              <span>{{ user.nickname }}</span>
              <el-button link type="primary" @click="handleLogout">退出</el-button>
            </div>
        </header>
        <div class="body">
            <aside class="sidebar">
                <RouterLink to="/">首页</RouterLink>
                <RouterLink to="/tickets">工单列表</RouterLink>
                <RouterLink v-if="isPresales" to="/tickets/create">创建工单</RouterLink>
            </aside>

            <main class="main">
                <router-view></router-view>
            </main>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
    const router = useRouter()
    const user = JSON.parse(localStorage.getItem('user')||'{}')
    const isPresales = user.role === 'presales'
    function handleLogout(){
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.success("已退出登录")
      router.push('/login')
    }
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ddd;
}

.body {
  flex: 1;
  display: flex;
}

.sidebar {
  width: 180px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #fff;
  border-right: 1px solid #ddd;
}

.main {
  flex: 1;
  padding: 20px;
}
@media (max-width: 768px) {
    .body {
        flex-direction: column;
    }
    .sidebar {
        width: 100%;
        flex-direction: row;
        overflow-x: auto;
        border-right: none;
        border-bottom: 1px solid #ddd;
    }
    .header {
        padding: 0 12px;
    }
    .main {
        padding: 12px;
    }
}
</style>