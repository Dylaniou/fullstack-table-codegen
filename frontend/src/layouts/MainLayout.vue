<template>
  <el-container>
    <!-- 顶部导航-->
    <el-header class="header">
      <div class="header-content">
        <el-divider direction="vertical" class="header-divider" />
        <div class="header-title">
          <el-icon><Cpu /></el-icon>
          <span>代码自动生成</span>
        </div>
        <el-divider direction="vertical" class="header-divider" />
      </div>
    </el-header>
    
    <el-container>
      <!-- 侧边 -->
      <el-aside width="200px" class="aside">
        <el-menu 
          :default-active="$route.path"
          class="el-menu-vertical-demo"
          router
        >
          <el-menu-item index="/">
            <el-icon><Home /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/tables">
            <el-icon><Database /></el-icon>
            <span>数据库表</span>
          </el-menu-item>
          <el-menu-item index="/codegenerator">
            <el-icon><Code /></el-icon>
            <span>代码生成</span>
          </el-menu-item>
          
          
                                                                                                              
          
                                                            <el-menu-item index="/user">
            <el-icon><Code /></el-icon>
            <span>User Management</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main class="main">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { Cpu, Home, Database, Code } from '@element-plus/icons-vue'

export default {
  name: 'MainLayout',
  components: {
    Cpu,
    Home,
    Database,
    Code
  }
}
</script>

<style scoped>
/* 头部样式 */
.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.header-divider {
  height: 30px;
  margin: 0 10px;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-title .el-icon {
  margin-right: 8px;
}

/* 侧边栏样�???????? */
.aside {
  background-color: #fff;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.05);
}

.el-menu-vertical-demo {
  border-right: none;
}

/* 主内容区域样�???????? */
.main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow: auto;
}
</style>
