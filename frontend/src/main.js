import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'

// 创建Vue应用
const app = createApp(App)

// 配置Element Plus
app.use(ElementPlus)

// 配置路由
app.use(router)

// 配置axios
app.config.globalProperties.$axios = axios

// 设置axios基础URL
axios.defaults.baseURL = '/'

// 响应拦截器处理错误
axios.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    if (error.response) {
      ElementPlus.ElMessage.error(error.response.data.message || '请求失败')
    } else {
      ElementPlus.ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

// 挂载应用
app.mount('#app')