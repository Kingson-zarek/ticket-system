<template>
<div v-loading="loading">
    <h1>工单详情</h1>
    <p>工单 ID：{{ id }}</p>
    <el-result v-if="errorMessage" icon="warning" :title="errorMessage">
        <template #extra>
            <RouterLink to="/tickets">返回列表</RouterLink>
        </template>
    </el-result>
    <template v-else>
    <el-card v-if="ticket">
        <h2>{{ ticket.title }}</h2>
        <p>工单号：{{ ticket.ticketNo }}</p>
        <p>
            状态：
            <el-tag :type="statusType(ticket.status)">
                {{statusText(ticket.status) }}
            </el-tag>
        </p>
        <p>
            优先级：
            <el-tag :type="priorityType(ticket.priority)">
                 {{ priorityText(ticket.priority) }}
            </el-tag>
           
        </p>
        <p>客户：{{ ticket.customerName }}</p>
        <p>处理人：{{ ticket.assigneeName || '未指派' }}</p>
        <p>创建时间：{{ ticket.createdAt }}</p>
        <p>内容：{{ ticket.content }}</p>
    </el-card>
    <el-card v-if="comments.length >0" style="margin-top: 16px;">
        <h3>评论</h3>
        <div v-for="comment in comments" :key="comment.id" class="comment">
            <p class="comment-head">
                <strong>{{ comment.userNickname }}</strong>
                <el-tag size="small" :type="roleType(comment.userRole)">
                    {{ roleText(comment.userRole) }}
                </el-tag>
                <span class="time">{{ comment.createdAt }}</span>
            </p>
            <p class="comment-content">{{ comment.content }}</p>
        </div>
    </el-card>
    <el-empty v-else description="暂无评论" style="margin-top: 16px;">

    </el-empty>
    <el-card style="margin-top: 16px;">
        <h3>发表评论</h3>
        <el-input v-model="commentContent" type="textarea" :rows="3" :maxlength="1000" show-word-limit="" placeholder="请输入评论内容"/>
       <el-button type="primary" style="margin-top: 10px;" :loading="submitting" :disabled="!commentContent.trim() || submitting" @click="submitComment">
        发表评论
       </el-button>
       </el-card>
    <RouterLink to="/tickets">返回列表</RouterLink>
    </template>
</div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import { getTicket } from '../api/ticket'
import { onMounted,ref } from 'vue';
import {getComments,addComment} from '../api/comment.js'
import { ElMessage } from 'element-plus';
    const route=useRoute()
    const id = route.params.id
    const ticket=ref(null)
    const loading=ref(true)
    const comments=ref([])
    const errorMessage=ref('')
    const commentContent=ref('')
    const submitting=ref(false)

    const roleMap = {
        presales:'售前',
        aftersales:'售后'
    }
    const roleTypeMap = {
        presales:'primary',
        aftersales:'success'
    }
    function roleText(role){
        return roleMap[role] || role
    }
    function roleType(role){
        return roleTypeMap[role] || 'info'
    }
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
    async function loadTicket() {
        errorMessage.value=''
       try{
         const data=await getTicket(id)
        ticket.value=data
    }catch(error){
        console.error(error)
        errorMessage.value=error.message || '加载工单失败'
    }finally{
        loading.value=false
    }}
    async function loadComments() {
        try{
         const data=await getComments(id)
        comments.value=data
    }catch(error){
        console.error(error)
    }
    }
    async function submitComment() {
        const content=commentContent.value.trim()
        if(!content){
            ElMessage.warning("请输入评论内容")
            return 
        }
        submitting.value=true
        try{
            await addComment(id,content)
            ElMessage.success("评论成功")
            commentContent.value=''
            await loadComments()
        }catch(error){
            console.error(error)
        }finally{
            submitting.value=false
        }
    }
    onMounted(async() =>{
        await loadTicket()
        if(!errorMessage.value){
             loadComments()
        }
       
    })
</script>

<style>
.comment {
    padding: 12px 0;
    border-bottom: 1px solid #eee;
}

.comment:last-child {
    border-bottom: none;
}

.comment-head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 0 6px;
}

.comment-content {
    margin: 0;
    line-height: 1.6;
}

.time {
    color: #999;
    font-size: 12px;
}
</style>