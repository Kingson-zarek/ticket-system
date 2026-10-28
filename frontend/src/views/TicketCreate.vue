<template>
    <h1>新建工单</h1>
    <div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="标题" prop="title" >
                <el-input v-model="form.title"/>
            </el-form-item>
            <el-form-item label="问题描述" prop="content" >
                <el-input type="textarea" :rows="3" v-model="form.content"/>
            </el-form-item>
            <el-form-item label="优先级" prop="priority" >
                <el-select v-model="form.priority" >
                    <el-option value="LOW" label="低"/>
                    <el-option value="NORMAL" label="普通"/>
                    <el-option value="HIGH" label="高"/>
                    <el-option value="URGENT" label="紧急"/>
                </el-select>
            </el-form-item>
            <el-form-item label="客户名称" prop="customerName" >
                <el-input v-model="form.customerName"/>
            </el-form-item>
            <el-form-item label="处理人" prop="assigneeId" >
                <el-select v-model="form.assigneeId">
                    <el-option v-for="u in aftersales" :key="u.id" :label="u.nickname" :value="u.id"></el-option>
                </el-select>
            </el-form-item>
            <el-button type="primary" :loading="loading" @click="createNewTicket">提交</el-button>
            <el-button @click="router.back()">取消</el-button>
        </el-form>
    </div>

</template>

<script setup>
import { useRouter } from 'vue-router';
import {ref,onMounted} from 'vue'
import { createTicket } from '../api/ticket';
import { getAftersales } from '../api/user';
import { ElMessage } from 'element-plus';

const formRef = ref(null)
const rules = {
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { max: 200, message: '标题最多200字', trigger: 'blur' }
    ],
    content: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
    assigneeId: [{ required: true, message: '请选择处理人', trigger: 'change' }]
}
const form = ref({title:'',content:'',priority:'NORMAL',customerName:'',assigneeId:null})
const aftersales = ref([])
const loading = ref(false)
const router = useRouter()


 async function createNewTicket(){
     try {
        await formRef.value.validate()
    } catch {
        return
    }
    loading.value=true
    try{
        const data = await createTicket(form.value)
        ElMessage.success(`建单成功，工单号 ${data.ticketNo}`)
        router.push(`/tickets/${data.id}`)
    }catch(error){
        console.error(error)
    }finally{
        loading.value=false
    }
}
onMounted(async()=>{
    try{
        aftersales.value = await getAftersales()
    }catch{
        aftersales.value = []
    }
})
</script>

<style>

</style>