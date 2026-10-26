import axios from "axios";
import { ElMessage } from "element-plus";

const request = axios.create({
    baseURL: '/api',
    timeout: 10000,
})

request.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if(token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

request.interceptors.response.use(
    (response) => {
        const data = response.data
        if(data.code === 0) return data.data
        if(data.code === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message))
    },
    (error) => {
        if(error.response?.status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        ElMessage.error('网络或服务异常')
        return Promise.reject(error)
    }
)


export default request