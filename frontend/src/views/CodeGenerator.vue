<template>
  <div class="code-generator-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>代码生成器</span>
        </div>
      </template>
      
      <!-- 配置区域 -->
      <div class="config-section">
        <el-form ref="form" :model="codeGenConfig" label-width="120px" class="form-config">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="数据库Schema">
                <el-input v-model="codeGenConfig.schemaName" placeholder="请输入数据库Schema名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="数据表">
                <el-select
                  v-model="codeGenConfig.tableName"
                  placeholder="请选择数据表"
                  filterable
                  style="width: 100%"
                  @focus="fetchTables"
                >
                  <el-option
                    v-for="table in tables"
                    :key="table.tableName"
                    :label="table.tableName"
                    :value="table.tableName"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="生成类型">
                <el-radio-group v-model="codeGenConfig.generateType">
                  <el-radio label="backend">仅后端代码</el-radio>
                  <el-radio label="frontend">仅前端代码</el-radio>
                  <el-radio label="fullstack">全栈代码</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="输出路径">
                <el-input v-model="codeGenConfig.outputPath" placeholder="默认使用配置文件路径" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="基础包名">
                <el-input v-model="codeGenConfig.basePackage" placeholder="默认使用配置文件包名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="覆盖已有文件">
                <el-switch v-model="codeGenConfig.overwrite" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="generateCode" :loading="loading">
            <el-icon><Code /></el-icon>
            生成代码
          </el-button>
          <el-button @click="deleteGeneratedCode" :disabled="!codeGenConfig.tableName || loading" type="danger">
            <el-icon><Delete /></el-icon>
            删除代码
          </el-button>
          <el-button @click="previewMetadata" :disabled="!codeGenConfig.tableName || loading">
            <el-icon><View /></el-icon>
            预览表结构
          </el-button>
        </div>
      </div>
      
      <!-- 表结构预览区域 -->
      <div v-if="showMetadataPreview" class="metadata-preview">
        <el-divider />
        <h3>表结构预览: {{ codeGenConfig.tableName }}</h3>
        
        <el-table
          v-loading="metadataLoading"
          :data="tableColumns"
          style="width: 100%"
          max-height="400"
        >
          <el-table-column
            prop="columnName"
            label="字段名"
            width="180"
          />
          <el-table-column
            prop="dataType"
            label="数据类型"
            width="120"
          />
          <el-table-column
            prop="javaType"
            label="Java类型"
            width="120"
          />
          <el-table-column
            prop="description"
            label="描述"
          />
          <el-table-column
            prop="isPrimaryKey"
            label="是否主键"
            width="100"
            align="center"
          >
            <template #default="scope">
              <el-tag v-if="scope.row.isPrimaryKey" type="primary">是</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="nullable"
            label="允许为空"
            width="100"
            align="center"
          >
            <template #default="scope">
              <el-tag v-if="scope.row.nullable" type="success">是</el-tag>
              <el-tag v-else type="danger">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="autoIncrement"
            label="自增"
            width="100"
            align="center"
          >
            <template #default="scope">
              <el-tag v-if="scope.row.autoIncrement" type="info">是</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 生成结果区域 -->
      <div v-if="showResult" class="result-section">
        <el-divider />
        <h3>生成结果</h3>
        
        <el-steps :active="resultStep" finish-status="success">
          <el-step title="准备阶段" />
          <el-step title="生成代码" />
          <el-step title="更新配置" />
          <el-step title="完成" />
        </el-steps>
        
        <div class="result-content">
          <pre v-if="resultMessage">{{ resultMessage }}</pre>
        </div>
        
        <div v-if="resultFiles.length > 0" class="result-files">
          <h4>生成的文件:</h4>
          <el-tree
            :data="resultFiles"
            show-checkbox
            default-expand-all
            node-key="id"
            :props="treeProps"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { Code, View, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { ref, reactive } from 'vue'
import axios from 'axios'

export default {
  name: 'CodeGenerator',
  components: {
    Code,
    View,
    Delete
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const metadataLoading = ref(false)
    const tables = ref([])
    const tableColumns = ref([])
    const showMetadataPreview = ref(false)
    const showResult = ref(false)
    const resultStep = ref(0)
    const resultMessage = ref('')
    const resultFiles = ref([])
    
    // 代码生成配置
    const codeGenConfig = reactive({
      schemaName: '',
      tableName: '',
      generateType: 'fullstack',
      outputPath: '',
      basePackage: '',
      overwrite: false
    })
    
    // 树结构属性配置
    const treeProps = {
      children: 'children',
      label: 'name'
    }
    
    // 获取数据表列表
    const fetchTables = async () => {
      if (!codeGenConfig.schemaName.trim()) {
        ElMessage.warning('请输入数据库Schema名称')
        return
      }
      
      try {
        const response = await axios.get(`/api/codegen/tables/${codeGenConfig.schemaName}`)
        // 将返回的表名数组转换为对象数组
        tables.value = response.map(tableName => ({
          tableName
        }))
      } catch (error) {
        console.error('获取数据表列表失败:', error)
        ElMessage.error('获取数据表列表失败')
      }
    }
    
    // 预览表结构
    const previewMetadata = async () => {
      if (!codeGenConfig.schemaName.trim() || !codeGenConfig.tableName.trim()) {
        ElMessage.warning('请输入Schema名称并选择数据表')
        return
      }
      
      metadataLoading.value = true
      try {
        const response = await axios.get(`/api/codegen/metadata/${codeGenConfig.schemaName}/${codeGenConfig.tableName}`)
        tableColumns.value = response.columns || []
        showMetadataPreview.value = true
      } catch (error) {
        console.error('获取表结构失败:', error)
        ElMessage.error('获取表结构失败')
      } finally {
        metadataLoading.value = false
      }
    }
    
    // 删除生成的代码
    const deleteGeneratedCode = async () => {
      if (!codeGenConfig.schemaName.trim() || !codeGenConfig.tableName.trim()) {
        ElMessage.warning('请输入Schema名称并选择数据表')
        return
      }
      
      // 确认删除操作
      if (!confirm(`确定要删除 ${codeGenConfig.schemaName}.${codeGenConfig.tableName} 生成的代码吗？此操作不可恢复。`)) {
        return
      }
      
      loading.value = true
      showResult.value = true
      resultStep.value = 0
      resultMessage.value = ''
      resultFiles.value = []
      
      try {
        // 准备阶段
        resultMessage.value += '准备删除代码...\n'
        resultStep.value = 1
        
        // 构建API URL
        const apiUrl = `/api/codegen/delete/${codeGenConfig.schemaName}/${codeGenConfig.tableName}`
        
        // 构建请求参数
        const requestParams = {
          deleteType: codeGenConfig.generateType,
          outputPath: codeGenConfig.outputPath
        }
        
        // 删除代码阶段
        resultMessage.value += `正在删除${getGenerateTypeText(codeGenConfig.generateType)}代码...\n`
        resultStep.value = 2
        
        // 发送请求
        await axios.delete(apiUrl, {
          data: requestParams
        })
        
        // 更新配置阶段
        resultMessage.value += '正在更新配置文件...\n'
        resultStep.value = 3
        
        // 完成阶段
        resultMessage.value += '代码删除成功！\n'
        
        ElMessage.success('代码删除成功')
      } catch (error) {
        console.error('删除代码失败:', error)
        resultMessage.value += `删除代码失败: ${error.message || '未知错误'}\n`
        ElMessage.error('删除代码失败')
      } finally {
        loading.value = false
      }
    }
    
    // 生成代码
    const generateCode = async () => {
      if (!codeGenConfig.schemaName.trim() || !codeGenConfig.tableName.trim()) {
        ElMessage.warning('请输入Schema名称并选择数据表')
        return
      }
      
      loading.value = true
      showResult.value = true
      resultStep.value = 0
      resultMessage.value = ''
      resultFiles.value = []
      
      try {
        // 准备阶段
        resultMessage.value += '准备生成代码...\n'
        resultStep.value = 1
        
        // 构建API URL
        let apiUrl = ''
        switch (codeGenConfig.generateType) {
          case 'backend':
            apiUrl = `/api/codegen/generate/backend/${codeGenConfig.schemaName}/${codeGenConfig.tableName}`
            break
          case 'frontend':
            apiUrl = `/api/codegen/generate/frontend/${codeGenConfig.schemaName}/${codeGenConfig.tableName}`
            break
          case 'fullstack':
          default:
            apiUrl = `/api/codegen/generate/fullstack/${codeGenConfig.schemaName}/${codeGenConfig.tableName}`
            break
        }
        
        // 构建请求参数
        const requestParams = {
          outputPath: codeGenConfig.outputPath,
          basePackage: codeGenConfig.basePackage,
          overwrite: codeGenConfig.overwrite
        }
        
        // 生成代码阶段
        resultMessage.value += `正在生成${getGenerateTypeText(codeGenConfig.generateType)}代码...\n`
        resultStep.value = 2
        
        // 发送请求
        const response = await axios.post(apiUrl, requestParams)
        
        // 更新配置阶段
        resultMessage.value += '正在更新配置文件...\n'
        resultStep.value = 3
        
        // 完成阶段
        resultMessage.value += '代码生成成功！\n'
        resultMessage.value += `生成的文件数量: ${response.generatedFiles?.length || 0}\n`
        
        // 构建文件树
        if (response.generatedFiles && response.generatedFiles.length > 0) {
          resultFiles.value = buildFileTree(response.generatedFiles)
        }
        
        ElMessage.success('代码生成成功')
      } catch (error) {
        console.error('生成代码失败:', error)
        resultMessage.value += `生成代码失败: ${error.message || '未知错误'}\n`
        ElMessage.error('生成代码失败')
      } finally {
        loading.value = false
      }
    }
    
    // 获取生成类型文本
    const getGenerateTypeText = (type) => {
      switch (type) {
        case 'backend':
          return '后端'
        case 'frontend':
          return '前端'
        case 'fullstack':
          return '全栈'
        default:
          return ''
      }
    }
    
    // 构建文件树结构
    const buildFileTree = (files) => {
      const fileMap = {}
      
      // 先创建所有节点
      files.forEach((file, index) => {
        const parts = file.split('/')
        let currentLevel = fileMap
        
        parts.forEach((part, i) => {
          if (!currentLevel[part]) {
            currentLevel[part] = {
              id: `${index}-${i}`,
              name: part,
              children: {}
            }
          }
          
          currentLevel = currentLevel[part].children
        })
      })
      
      // 递归转换为数组结构
      const mapToTree = (nodeMap) => {
        return Object.values(nodeMap).map(node => ({
          id: node.id,
          name: node.name,
          children: mapToTree(node.children)
        })).filter(item => item.children.length > 0 || files.some(file => file.endsWith(item.name)))
      }
      
      return mapToTree(fileMap)
    }
    
    return {
      loading,
      metadataLoading,
      tables,
      tableColumns,
      showMetadataPreview,
      showResult,
      resultStep,
      resultMessage,
      resultFiles,
      codeGenConfig,
      treeProps,
      fetchTables,
      previewMetadata,
      generateCode,
      deleteGeneratedCode
    }
  }
}
</script>

<style scoped>
.code-generator-container {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-section {
  margin-bottom: 20px;
}

.form-config {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.metadata-preview {
  margin-top: 20px;
}

.metadata-preview h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.result-section {
  margin-top: 20px;
}

.result-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.result-content {
  margin: 20px 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow-x: auto;
}

.result-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', Courier, monospace;
}

.result-files {
  margin-top: 20px;
}

.result-files h4 {
  margin-bottom: 10px;
  font-size: 14px;
  color: #333;
}
</style>