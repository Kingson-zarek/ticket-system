<template>
    <h1>首页看板</h1>
    <div v-loading="loading">
        <el-result v-if="errorMessage" icon="warning" :title="errorMessage">
            <template #extra>
                <el-button type="primary" @click="loadSummary">重新加载</el-button>
            </template>
        </el-result>
        <template v-else>
            <el-row :gutter="16">
                <el-col :xs="12" :sm="6" v-for="card in cards" :key="card.key">
                    <el-card shadow="hover" class="stat-card">
                        <p class="stat-label">{{ card.label }}</p>
                        <p class="stat-value" :style="{ color: card.color }">
                            {{ summary[card.key] ?? 0 }}
                        </p>
                    </el-card>
                </el-col>
            </el-row>
            <el-card class="stat-extra">
                <p>今日新增：{{ summary.todayNew ?? 0 }}</p>
                <p>平均处理时长：{{ (summary.avgHandleHours ?? 0).toFixed(1) }} 小时</p>
            </el-card>
        </template>
    </div>
</template>

<script setup>
import { getSummary } from '../api/report';
import {ref,onMounted} from 'vue'
const cards = [
    { key: 'open', label: '待处理', color: '#f56c6c' },
    { key: 'processing', label: '处理中', color: '#e6a23c' },
    { key: 'resolved', label: '已解决', color: '#67c23a' },
    { key: 'closed', label: '已关闭', color: '#909399' }
]
const summary=ref({})
const loading=ref(false)
const errorMessage=ref('')

async function loadSummary() {
    loading.value=true
    errorMessage.value=''
    try{
        summary.value = await getSummary()
       
    }catch(error){
         console.error(error)
        errorMessage.value='统计加载失败'
    }finally{
        loading.value=false
    }
}
 onMounted(loadSummary)
</script>

<style>
.stat-card {
    margin-bottom: 16px;
}
.stat-label {
    margin: 0;
    color: #909399;
    font-size: 13px;
}
.stat-value {
    margin: 8px 0 0;
    font-size: 28px;
    font-weight: 600;
}
.stat-extra {
    margin-top: 8px;
}
</style>