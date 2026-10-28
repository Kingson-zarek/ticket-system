<template>
    <div class="list-header">
        <div>
            <h1>工单列表</h1>
        <p>共 {{total}} 条</p>
        </div>
        <el-button v-if="isPresales" type="primary" @click="router.push('/tickets/create')">
            新建工单
        </el-button>
    </div>
    <el-table :data="tickets" border stripe @row-click="handleRowClick">
        <el-table-column prop="ticketNo" label="工单号" width="170"/>
        <el-table-column prop="title" label="标题" min-width="200"/>
        <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
                <el-tag :type="statusType(row.status)">
                    {{ statusText(row.status) }}
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
            <template #default="{ row }">
                <el-tag :type="priorityType(row.priority)">
                    {{ priorityText(row.priority) }}
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="处理人" width="120"/>
        <el-table-column prop="createdAt" label="创建时间" width="180"/>
    </el-table>
    <el-pagination 
        v-model:current-page="page"
        v-model:page-size='size'
        :total="total"
        layout="prev,pager,next"
        background
        @current-change="loadTickets"
        @size-change="handleSizeChange"
    />
</template>

<script setup>
import {onMounted,  ref } from 'vue'
import { getTickets } from '../api/ticket';
import { useRouter } from 'vue-router';
    const total=ref(0)
    const tickets=ref([])
    const page=ref(1)
    const size=ref(10)
    const router=useRouter()
    const user = JSON.parse(localStorage.getItem('user') || {})
    const isPresales = user.role ==='presales'
    const statusMap = {
        OPEN: '待处理',
        PROCESSING: '处理中',
        RESOLVED: '已解决',
        CLOSED: '已关闭'
}
    const statusTypeMap = {
        OPEN: 'danger',
        PROCESSING: 'warning',
        RESOLVED: 'success',
        CLOSED: 'info'
    }
    const priorityMap = {
        LOW: '低',
        NORMAL: '普通',
        HIGH: '高',
        URGENT: '紧急'
}

    const priorityTypeMap = {
        LOW: 'info',
        NORMAL: 'primary',
        HIGH: 'warning',
        URGENT: 'danger'
}
    function statusText(status) {
        return statusMap[status] || status
}

    function statusType(status) {
        return statusTypeMap[status] || 'info'
}

    function priorityText(priority) {
        return priorityMap[priority] || priority
}

    function priorityType(priority) {
        return priorityTypeMap[priority] || 'info'
}
    async function loadTickets() {
        const data = await getTickets({
            page:page.value,
            size:size.value
        })
        console.log(data)
        total.value=data.total
        tickets.value=data.records
    }
    function handleSizeChange(){
        page.value=1
        loadTickets()
    }
    function handleRowClick(row){
        router.push(`/tickets/${row.id}`)
    }
    onMounted(loadTickets)
</script>


<style>
.el-table__row {
  cursor: pointer;
}
.list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

</style>