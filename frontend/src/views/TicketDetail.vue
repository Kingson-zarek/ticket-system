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
        <div class="actions">
            <el-button v-if="ticket.status === 'OPEN' && isAssignee" type="primary" :loading="actionLoading" @click="changeStatus('PROCESSING')">
                接单
            </el-button>
            <el-button v-if="ticket.status === 'PROCESSING' && isAssignee" type="primary" :loading="actionLoading" @click="changeStatus('RESOLVED')">
                标记解决
            </el-button>
            <el-button v-if="canClose" :loading="actionLoading" @click="confirmClose">
                关闭
            </el-button>
            <el-button v-if="ticket.status === 'RESOLVED' && isCreator && isPresales" :loading="actionLoading" @click="changeStatus('PROCESSING')">
                退回处理中
            </el-button>
            <el-button v-if="ticket.status !== 'CLOSED' && isCreator && isPresales" @click="openAssign">
                改派
            </el-button>
            <el-button v-if="ticket.status !== 'CLOSED' && isCreator" type="danger" @click="confirmDelete">
                删除
            </el-button>
        </div>
            <el-dialog v-model="assignVisible" title="改派处理人" width="360px">
            <el-select v-model="assignId" placeholder="请选择处理人" style="width: 100%">
                <el-option v-for="u in aftersales" :key="u.id" :label="u.nickname" :value="u.id"/>
            </el-select>
            <template #footer>
                <el-button @click="assignVisible = false">取消</el-button>
                <el-button type="primary" :loading="assigning" @click="submitAssign">确定</el-button>
            </template>
        </el-dialog>
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
import { useRoute,useRouter } from 'vue-router';
import { getTicket, updateStatus,deleteTicket,assignTicket } from '../api/ticket'
import { computed,onMounted,ref } from 'vue';
import {getComments,addComment} from '../api/comment.js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { getAftersales } from '../api/user.js';
    const route=useRoute()
    const router=useRouter()
    const id = route.params.id
    const ticket=ref(null)
    const loading=ref(true)
    const comments=ref([])
    const errorMessage=ref('')
    const commentContent=ref('')
    const submitting=ref(false)
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const uid = user.userId
    const isPresales = user.role === 'presales'
    const isCreator = computed(()=> ticket.value?.createdBy === uid)
    const isAssignee = computed(()=> ticket.value?.assigneeId === uid)
    const actionLoading = ref(false)
    const canClose = computed(()=>
        (ticket.value?.status === 'PROCESSING' && (isCreator.value || isAssignee.value)) ||
        (ticket.value?.status === 'RESOLVED' && isCreator.value && isPresales)
    )
    const assignVisible = ref(false)
    const assignId = ref(null)
    const aftersales = ref([])
    const assigning = ref(false)

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
    async function changeStatus(status){
        actionLoading.value=true
        try{
            await updateStatus(id,status)
            ElMessage.success("修改状态成功")
            await loadTicket()
        }catch(error){
            console.error(error)
        }finally{
            actionLoading.value=false
        }
    }

    async function confirmClose() {
        try{
            await ElMessageBox.confirm('关闭后不能再流转，确定关闭？','关闭确认',
            { type: 'warning', confirmButtonText: '关闭', cancelButtonText: '取消'})
        }catch{
            return
        }
        await changeStatus('CLOSED')
    }

    async function openAssign() {
        assignId.value = ticket.value?.assigneeId ?? null
        assignVisible.value = true
        try {
            aftersales.value = await getAftersales()
        } catch {
            aftersales.value = []
        }
    }

    async function submitAssign() {
        if (!assignId.value) {
            ElMessage.warning('请选择处理人')
            return
        }
        assigning.value = true
        try {
            await assignTicket(id, assignId.value)
            ElMessage.success('改派成功')
            assignVisible.value = false
            await loadTicket()
        } catch (error) {
            console.error(error)
        } finally {
            assigning.value = false
        }
    }

    async function confirmDelete() {
        try{
            await ElMessageBox.confirm(`确定删除工单 ${ticket.value.ticketNo}？`,'删除确认',
                {type: 'warning',confirmButtonText:'删除',cancelButtonText:'取消'}
            )
        }catch{
            return
        }
        actionLoading.value=true
        try{
            await deleteTicket(id)
            ElMessage.success('已删除')
            router.push('/tickets')
        }catch(error){
            console.error(error)
        }finally{
            actionLoading.value=false
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
.actions {
    display: flex;
    gap: 8px;
    margin: 12px 0;
}

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