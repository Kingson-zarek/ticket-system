<template>
    <div class="login-page">
        <el-card class="login-card">
            <h2>客服工单系统</h2>
            <el-form>
                <el-form-item >
                    <el-input placeholder="用户名" v-model="username"/>
                </el-form-item>

                <el-form-item>
                    <el-input type="password" placeholder="密码" v-model="password" show-password/>
                </el-form-item>
                <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">
                    登录
                </el-button>
            </el-form>
        </el-card>
    </div>
</template>

<script setup>
import {ref} from 'vue'
import { ElMessage } from 'element-plus'
import {login} from '../api/auth.js'
import { useRouter } from 'vue-router'

    const username=ref('')
    const password=ref('')
    const router = useRouter()
    const loading = ref(false)
    async function handleLogin(){
         if(!username.value || !password.value){
            ElMessage.warning("请输入用户名和密码")
            return
        }
        loading.value=true
        try{
           
        const data = await login({
            username:username.value,
            password:password.value
        })
        localStorage.setItem('token',data.token)
        localStorage.setItem('user',JSON.stringify({
            userId:data.userId,
            nickname:data.nickname,
            role:data.role
        }))
        ElMessage.success("登录成功")
        router.push("/")
        } catch(error){
            console.error(error)
        }finally{
            loading.value=false
        }
        
    }
</script>

<style scoped>
.login-page{
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
}

.login-card{
    width: 360px;
}
</style>