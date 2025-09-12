<template>
  <div class="table-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据库表列表</span>
        </div>
      </template>
      
      <!-- 搜索和操作区域 -->
      <div class="search-actions">
        <el-input
          v-model="schemaName"
          placeholder="输入数据库Schema名称"
          style="width: 300px; margin-right: 10px;"
        />
        <el-button type="primary" @click="fetchTables">
          <el-icon><Search /></el-icon>
          刷新列表
        </el-button>
      </div>
      
      <!-- 表列表 -->
      <div class="table-content">
        <el-table
          v-loading="loading"
          :data="tables"
          style="width: 100%"
          @row-click="handleRowClick"
        >
          <el-table-column
            prop="tableName"
            label="表名"
            width="300"
          />
          <el-table-column
            prop="description"
            label="描述"
            show-overflow-tooltip
          />
          <el-table-column
            label="操作"
            fixed="right"
            width="250"
          >
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click.stop="generateCode(scope.row)"
                style="margin-right: 10px;"
              >
                生成代码
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click.stop="deleteGeneratedCode(scope.row)"
              >
                删除代码
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 空状态 -->
        <div v-if="!loading && tables.length === 0" class="empty-state">
          <el-empty description="暂无数据表" />
        </div>
      </div>
    </el-card>
    
    <!-- 生成代码对话框 -->
    <el-dialog
      title="生成代码选项"
      v-model="dialogVisible"
      width="600px"
    >
      <div class="dialog-content">
        <p class="selected-table">已选择表: {{ selectedTable?.tableName || '' }}</p>
        
        <el-form ref="form" :model="codeGenForm" label-width="120px">
          <el-form-item label="生成类型">
            <el-radio-group v-model="codeGenForm.generateType">
              <el-radio label="backend">仅后端代码</el-radio>
              <el-radio label="frontend">仅前端代码</el-radio>
              <el-radio label="fullstack">全栈代码</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="代码输出路径">
            <el-input v-model="codeGenForm.outputPath" placeholder="默认使用配置文件路径" />
          </el-form-item>
          
          <el-form-item label="包名">
            <el-input v-model="codeGenForm.basePackage" placeholder="默认使用配置文件包名" />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="confirmGenerateCode">
          确认生成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'TableList',
  components: {
    Search
  },
  setup() {
    // 响应式数据
    const schemaName = ref('')
    const tables = ref([])
    const loading = ref(false)
    const dialogVisible = ref(false)
    const selectedTable = ref(null)
    const codeGenForm = ref({
      generateType: 'fullstack',
      outputPath: '',
      basePackage: ''
    })
    
    // 获取数据表列表
    const fetchTables = async () => {
      if (!schemaName.value.trim()) {
        ElMessage.warning('请输入数据库Schema名称')
        return
      }
      
      loading.value = true
      try {
        const response = await axios.get(`/api/codegen/tables/${schemaName.value}`)
        // 将返回的表名数组转换为对象数组，方便表格显示
        tables.value = response.map(tableName => ({
          tableName,
          description: `数据表: ${tableName}`
        }))
      } catch (error) {
        console.error('获取数据表列表失败:', error)
        ElMessage.error('获取数据表列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 处理行点击事件
    const handleRowClick = (row) => {
      selectedTable.value = row
      dialogVisible.value = true
      // 重置表单
      codeGenForm.value = {
        generateType: 'fullstack',
        outputPath: '',
        basePackage: ''
      }
    }
    
    // 点击生成代码按钮
    const generateCode = (row) => {
      selectedTable.value = row
      dialogVisible.value = true
      // 重置表单
      codeGenForm.value = {
        generateType: 'fullstack',
        outputPath: '',
        basePackage: ''
      }
    }
    
    // 确认生成代码
    const confirmGenerateCode = async () => {
      if (!selectedTable.value) {
        ElMessage.warning('请选择数据表')
        return
      }
      
      loading.value = true
      try {
        let apiUrl = ''
        
        // 根据生成类型选择不同的API
        switch (codeGenForm.value.generateType) {
          case 'backend':
            apiUrl = `/api/codegen/generate/backend/${schemaName.value}/${selectedTable.value.tableName}`
            break
          case 'frontend':
            apiUrl = `/api/codegen/generate/frontend/${schemaName.value}/${selectedTable.value.tableName}`
            break
          case 'fullstack':
          default:
            apiUrl = `/api/codegen/generate/fullstack/${schemaName.value}/${selectedTable.value.tableName}`
            break
        }
        
        // 构建请求参数
        const requestParams = {
          outputPath: codeGenForm.value.outputPath,
          basePackage: codeGenForm.value.basePackage
        }
        
        // 发送请求
        await axios.post(apiUrl, requestParams)
        ElMessage.success('代码生成成功')
        dialogVisible.value = false
      } catch (error) {
        console.error('生成代码失败:', error)
        ElMessage.error('生成代码失败')
      } finally {
        loading.value = false
      }
    }
    
    // 删除生成的代码
    const deleteGeneratedCode = async (row) => {
      try {
        // 显示确认对话框
        await ElMessageBox.confirm(
          `确定要删除表 "${row.tableName}" 生成的代码吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        loading.value = true
        // 构建请求参数
        const requestParams = {
          deleteType: 'fullstack', // 默认删除全栈代码
          outputPath: '' // 使用默认路径
        }
        
        // 发送删除请求到后端
        await axios.delete(`/api/codegen/delete/${schemaName.value}/${row.tableName}`, {
          data: requestParams
        })
        
        ElMessage.success('代码删除成功')
      } catch (error) {
        // 如果用户取消删除，不显示错误消息
        if (error !== 'cancel') {
          console.error('删除代码失败:', error)
          ElMessage.error('删除代码失败')
        }
      } finally {
        loading.value = false
      }
    }
    
    // 组件挂载时的初始化
    onMounted(() => {
      // 默认显示第一个常用schema的表（如果有配置的话）
      // 这里可以根据实际情况修改
      // schemaName.value = 'default_schema'
      // fetchTables()
    })
    
    return {
      schemaName,
      tables,
      loading,
      dialogVisible,
      selectedTable,
      codeGenForm,
      fetchTables,
      handleRowClick,
      generateCode,
      confirmGenerateCode,
      deleteGeneratedCode
    }
  }
}
</script>

<style scoped>
.table-list-container {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-actions {
  display: flex;
  margin-bottom: 20px;
  padding: 10px 0;
}

.table-content {
  overflow-x: auto;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.dialog-content {
  padding: 10px 0;
}

.selected-table {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-weight: 500;
}
</style>