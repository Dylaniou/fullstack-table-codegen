<template>
  <div id="app">
    <main-layout />
  </div>
</template>

<script>
import MainLayout from './layouts/MainLayout.vue'

export default {
  name: 'App',
  components: {
    MainLayout
  }
}
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  background-color: #f5f7fa;
}

/* 应用容器样式 */
#app {
  height: 100vh;
  overflow: hidden;
}

.header-divider {
  height: 30px;
  background-color: #f0f0f0;
}

.header-title {
  display: flex;
  align-items: center;
  margin: 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.header-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

/* 侧边栏样式 */
.aside {
  background-color: #fff;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.el-menu-vertical-demo {
  height: 100%;
  border-right: none;
}

.el-menu-item {
  padding-left: 20px !important;
}

.el-menu-item.is-active {
  background-color: #e6f7ff !important;
  color: #1890ff !important;
}

/* 主内容区域样式 */
.main {
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .aside {
    display: none;
  }
  
  .main {
    padding: 10px;
  }
}
</style>