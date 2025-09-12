<template>
  <div class="home-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎使用代码自动生成器</h1>
        <p class="welcome-subtitle">快速生成高质量的前后端代码，提高开发效率</p>
        <div class="welcome-actions">
          <el-button type="primary" size="large" @click="navigateToCodeGenerator">
            <el-icon><Code /></el-icon>
            开始生成代码
          </el-button>
          <el-button size="large" @click="navigateToTableList">
            <el-icon><Database /></el-icon>
            查看数据库表
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 功能介绍区域 -->
    <div class="features-section">
      <h2 class="section-title">功能特点</h2>
      <div class="features-grid">
        <el-card class="feature-card">
          <div class="feature-icon">
            <el-icon><Database /></el-icon>
          </div>
          <h3 class="feature-title">数据库元数据解析</h3>
          <p class="feature-description">自动解析数据库表结构，识别字段类型、主键、外键等元数据信息</p>
        </el-card>
        
        <el-card class="feature-card">
          <div class="feature-icon">
            <el-icon><Code /></el-icon>
          </div>
          <h3 class="feature-title">后端代码生成</h3>
          <p class="feature-description">一键生成实体类、Repository、Controller等后端代码，支持Spring Boot框架</p>
        </el-card>
        
        <el-card class="feature-card">
          <div class="feature-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <h3 class="feature-title">前端代码生成</h3>
          <p class="feature-description">自动生成Vue组件、路由配置和导航链接，支持Element Plus组件库</p>
        </el-card>
        
        <el-card class="feature-card">
          <div class="feature-icon">
            <el-icon><RefreshRight /></el-icon>
          </div>
          <h3 class="feature-title">代码更新与合并</h3>
          <p class="feature-description">智能更新现有代码，保留用户自定义修改，避免代码覆盖</p>
        </el-card>
      </div>
    </div>
    
    <!-- 使用流程区域 -->
    <div class="workflow-section">
      <h2 class="section-title">使用流程</h2>
      <div class="workflow-steps">
        <div class="workflow-step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h3 class="step-title">连接数据库</h3>
            <p class="step-description">在配置文件中设置数据库连接信息</p>
          </div>
        </div>
        
        <div class="workflow-arrow">
          <el-icon><Right /></el-icon>
        </div>
        
        <div class="workflow-step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h3 class="step-title">选择数据表</h3>
            <p class="step-description">在数据库表页面查看并选择要生成代码的表</p>
          </div>
        </div>
        
        <div class="workflow-arrow">
          <el-icon><Right /></el-icon>
        </div>
        
        <div class="workflow-step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h3 class="step-title">生成代码</h3>
            <p class="step-description">在代码生成页面选择生成选项并生成代码</p>
          </div>
        </div>
        
        <div class="workflow-arrow">
          <el-icon><Right /></el-icon>
        </div>
        
        <div class="workflow-step">
          <div class="step-number">4</div>
          <div class="step-content">
            <h3 class="step-title">应用代码</h3>
            <p class="step-description">将生成的代码集成到您的项目中</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部信息区域 -->
    <div class="footer-section">
      <p>Code Generator &copy; 2023 - 提高开发效率，专注业务逻辑</p>
    </div>
  </div>
</template>

<script>
import { Code, Database, Monitor, RefreshRight, Right } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

export default {
  name: 'HomePage',
  components: {
    Code,
    Database,
    Monitor,
    RefreshRight,
    Right
  },
  setup() {
    const router = useRouter()
    
    const navigateToCodeGenerator = () => {
      router.push('/codegenerator')
    }
    
    const navigateToTableList = () => {
      router.push('/tables')
    }
    
    return {
      navigateToCodeGenerator,
      navigateToTableList
    }
  }
}
</script>

<style scoped>
/* 主容器样式 */
.home-container {
  width: 100%;
  min-height: 100%;
}

/* 欢迎区域样式 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 20px;
  text-align: center;
  margin-bottom: 40px;
}

.welcome-content {
  max-width: 800px;
  margin: 0 auto;
}

.welcome-title {
  font-size: 36px;
  margin-bottom: 16px;
  font-weight: 600;
}

.welcome-subtitle {
  font-size: 18px;
  margin-bottom: 32px;
  opacity: 0.9;
}

.welcome-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 功能介绍区域样式 */
.features-section {
  padding: 0 20px 40px;
}

.section-title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 32px;
  color: #333;
  font-weight: 600;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 36px;
  color: #1890ff;
  margin-bottom: 16px;
  text-align: center;
}

.feature-title {
  font-size: 18px;
  margin-bottom: 12px;
  text-align: center;
  color: #333;
  font-weight: 600;
}

.feature-description {
  font-size: 14px;
  color: #666;
  text-align: center;
  line-height: 1.6;
}

/* 使用流程区域样式 */
.workflow-section {
  background-color: #fafafa;
  padding: 40px 20px;
  margin-bottom: 40px;
}

.workflow-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 150px;
}

.step-number {
  width: 40px;
  height: 40px;
  background-color: #1890ff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 12px;
}

.step-content {
  text-align: center;
}

.step-title {
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
  font-weight: 600;
}

.step-description {
  font-size: 14px;
  color: #666;
}

.workflow-arrow {
  font-size: 20px;
  color: #999;
  margin: 0 5px;
}

/* 底部信息区域样式 */
.footer-section {
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 14px;
  background-color: white;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-title {
    font-size: 28px;
  }
  
  .welcome-subtitle {
    font-size: 16px;
  }
  
  .welcome-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .workflow-steps {
    flex-direction: column;
  }
  
  .workflow-arrow {
    transform: rotate(90deg);
    margin: 10px 0;
  }
}
</style>