import { createRouter, createWebHistory } from 'vue-router'

// 导入组件
const HomePage = () => import('../views/Home.vue')
const TableList = () => import('../views/TableList.vue')
const CodeGenerator = () => import('../views/CodeGenerator.vue')
const UserManagement = () => import('../views/UserManagement.vue')
// 定义路由配置
const routes = [
  {
    path: '/',
    name: 'HomePage',
    component: HomePage
  },
  {
    path: '/tables',
    name: 'TableList',
    component: TableList,
    meta: {
      title: '数据库表列表'
    }
  },
  {
    path: '/codegenerator',
    name: 'CodeGenerator',
    component: CodeGenerator,
    meta: {
      title: '代码生成器'
    }
  }
  ,
  {
    path: '/user',
    name: 'UserManagement',
    component: UserManagement
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由前置守卫，用于设置页面标题
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

// 导出路由实例
export default router
