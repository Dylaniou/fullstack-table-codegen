<template>
  <div class="user-container">
    <h2>User Management</h2>
    
    <!-- Search and Add Button Section -->
    <div class="top-actions">
      <input 
        type="text" 
        v-model="searchQuery"
        placeholder="Search..."
        class="search-input"
        @input="handleSearch"
      />
      <div class="top-buttons">
        <button @click="exportData" class="btn-export">
          Export
        </button>
        <input
          ref="fileInput"
          type="file"
          style="display: none;"
          accept=".xlsx,.xls,.csv"
          @change="handleFileUpload"
        />
        <button @click="triggerFileUpload" class="btn-import">
          Import
        </button>
        <button @click="batchDelete" class="btn-delete" :disabled="!Array.isArray(selectedIds) || selectedIds.length === 0">
          Batch Delete
        </button>
        <button @click="showAddModal" class="btn-add">
          Add User
        </button>
      </div>
    </div>
    
    <!-- Data Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th width="50">
              <input 
                type="checkbox" 
                v-model="selectAll"
                @change="handleSelectAll"
              />
            </th>
            <th v-for="column in displayColumns" :key="column.field">
              {{ column.label }}
            </th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>
              <input 
                type="checkbox" 
                v-model="selectedIds"
                :value="item.id"
              />
            </td>
            <td v-for="column in displayColumns" :key="column.field">
              {{ item[column.field] }}
            </td>
            <td class="action-buttons">
              <button @click="editItem(item)" class="btn-edit">
                Edit
              </button>
              <button @click="deleteItem(item.id)" class="btn-delete">
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Empty State -->
    <div v-if="(!Array.isArray(items) || items.length === 0) && !loading" class="empty-state">
      <p>No data available</p>
    </div>
    
    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <p>Loading...</p>
    </div>
    
    <!-- Pagination -->
    <div class="pagination" v-if="totalPages > 0">
      <button @click="changePage(1)" :disabled="currentPage === 1">First</button>
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">Previous</button>
      <span>Page {{ currentPage }} of {{ totalPages }}</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">Next</button>
      <button @click="changePage(totalPages)" :disabled="currentPage === totalPages">Last</button>
    </div>
    
    <!-- Form Modal -->
    <div v-if="showAddForm || showEditForm" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showEditForm ? 'Edit' : 'Add New' }} User</h3>
          <button @click="closeModal" class="modal-close">×</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="submitForm">
            <div v-for="column in formColumns" :key="column.field" class="form-group">
              <label :for="column.field">{{ column.label }}</label>
              <input 
                :id="column.field"
                v-model="formData[column.field]"
                :type="getColumnType(column)"
                :required="!column.nullable"
                class="form-input"
              />
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeModal" class="btn-cancel">
                Cancel
              </button>
              <button type="submit" class="btn-submit">
                {{ showEditForm ? 'Update' : 'Save' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'UserComponent',
  data() {
    return {
      items: [],
      searchQuery: '',
      showAddForm: false,
      showEditForm: false,
      formData: {},
      editingId: null,
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalItems: 0,
      selectedIds: [],
      selectAll: false,
      displayColumns: [
        {
          field: 'mobile',
          label: 'Mobile'
        },
        {
          field: 'passwd',
          label: 'Passwd'
        },
        {
          field: 'name',
          label: 'Name'
        },
        {
          field: 'sex',
          label: 'Sex'
        },
        {
          field: 'age',
          label: 'Age'
        },
        {
          field: 'birthday',
          label: 'Birthday'
        },
        {
          field: 'area',
          label: 'Area'
        },
        {
          field: 'score',
          label: 'Score'
        }
      ],
      formColumns: [
        {
          field: 'mobile',
          label: 'Mobile',
          nullable: false
        },
        {
          field: 'passwd',
          label: 'Passwd',
          nullable: false
        },
        {
          field: 'name',
          label: 'Name',
          nullable: true
        },
        {
          field: 'sex',
          label: 'Sex',
          nullable: true
        },
        {
          field: 'age',
          label: 'Age',
          nullable: true
        },
        {
          field: 'birthday',
          label: 'Birthday',
          nullable: true
        },
        {
          field: 'area',
          label: 'Area',
          nullable: true
        },
        {
          field: 'score',
          label: 'Score',
          nullable: true
        }
      ],
      apiEndpoint: '/api/user'
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.totalItems / this.pageSize);
    }
  },
  watch: {
    currentPage() {
      this.fetchData();
    },
    pageSize() {
      this.currentPage = 1;
      this.fetchData();
    },
    selectedIds: {
      handler(newVal) {
        // 如果所有项都被选中，则勾选全选框
        this.selectAll = Array.isArray(newVal) && Array.isArray(this.items) && newVal.length === this.items.length && this.items.length > 0;
      },
      deep: true
    }
  },
  mounted() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage - 1, // 后端通常从0开始计数
          size: this.pageSize
        };
        
        if (this.searchQuery) {
          params.search = this.searchQuery;
        }
        
        // axios拦截器已经直接返回了response.data，所以这里直接使用返回的数据
        const data = await axios.get(this.apiEndpoint, { params });
        // 假设后端返回的是Page对象，包含content和totalElements
        if (data && data.content) {
          this.items = Array.isArray(data.content) ? data.content : [];
          this.totalItems = data.totalElements || 0;
        } else {
          // 兼容旧版返回直接是列表的情况
          this.items = Array.isArray(data) ? data : [];
          this.totalItems = Array.isArray(data) ? data.length : 0;
        }
        // 清空选中状态
        this.selectedIds = [];
        this.selectAll = false;
      } catch (error) {
        console.error('Error fetching data:', error);
        alert('Failed to fetch data');
      } finally {
        this.loading = false;
      }
    },
    getColumnType(column) {
      if (column.field.toLowerCase().includes('email')) return 'email';
      if (column.field.toLowerCase().includes('password')) return 'password';
      if (column.field.toLowerCase().includes('date')) return 'date';
      if (column.field.toLowerCase().includes('time')) return 'time';
      if (column.field.toLowerCase().includes('datetime')) return 'datetime-local';
      return 'text';
    },
    showAddModal() {
      this.formData = {};
      this.showAddForm = true;
      this.editingId = null;
    },
    editItem(item) {
      this.formData = { ...item };
      this.editingId = item.id;
      this.showEditForm = true;
    },
    closeModal() {
      this.showAddForm = false;
      this.showEditForm = false;
      this.formData = {};
      this.editingId = null;
    },
    async submitForm() {
      try {
        if (this.showEditForm) {
          // 更新数据
          await axios.put(this.apiEndpoint + '/' + this.editingId, this.formData);
        } else {
          // 创建数据
          await axios.post(this.apiEndpoint, this.formData);
        }
        
        this.closeModal();
        this.fetchData();
      } catch (error) {
        console.error('Error submitting form:', error);
        alert('Failed to save data');
      }
    },
    async deleteItem(id) {
      if (confirm('Are you sure you want to delete this item?')) {
        try {
          await axios.delete(this.apiEndpoint + '/' + id);
          this.fetchData();
        } catch (error) {
          console.error('Error deleting item:', error);
          alert('Failed to delete item');
        }
      }
    },
    async batchDelete() {
      if (!Array.isArray(this.selectedIds) || this.selectedIds.length === 0) {
        alert('Please select items to delete');
        return;
      }
      
      if (confirm('Are you sure you want to delete ' + (Array.isArray(this.selectedIds) ? this.selectedIds.length : 0) + ' items?')) {
        try {
          await axios.delete(this.apiEndpoint + '/batch', {
            data: this.selectedIds
          });
          this.fetchData();
        } catch (error) {
          console.error('Error deleting items:', error);
          alert('Failed to delete items');
        }
      }
    },
    
    // 导出数据
    async exportData() {
      try {
        // 对于blob类型的响应，需要特殊处理，直接访问返回的blob数据
        // 注意：axios拦截器会直接返回response.data
        const response = await axios.get(this.apiEndpoint + '/export', {
          params: {
            search: this.searchQuery
          },
          responseType: 'blob'
        });
        
        // 创建下载链接
        // 由于axios拦截器直接返回了blob数据，所以直接使用response
        const url = window.URL.createObjectURL(new Blob([response]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'user-export.xlsx');
        document.body.appendChild(link);
        link.click();
        
        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch (error) {
        console.error('Error exporting data:', error);
        alert('Failed to export data');
      }
    },
    
    // 触发文件上传
    triggerFileUpload() {
      this.$refs.fileInput.click();
    },
    
    // 处理文件上传
    async handleFileUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        this.loading = true;
        await axios.post(this.apiEndpoint + '/import', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        
        alert('Import successful');
        this.fetchData();
      } catch (error) {
        console.error('Error importing data:', error);
        alert('Failed to import data');
      } finally {
        this.loading = false;
        // 重置文件输入
        event.target.value = '';
      }
    },
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    handleSearch() {
      this.currentPage = 1;
      // 使用防抖，避免频繁请求
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }
      this.searchTimeout = setTimeout(() => {
        this.fetchData();
      }, 300);
    },
    handleSelectAll() {
      if (this.selectAll && Array.isArray(this.items)) {
        // 全选
        this.selectedIds = this.items.map(item => item.id);
      } else {
        // 取消全选
        this.selectedIds = [];
      }  
    }
  }
}
</script>

<style scoped>
.user-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  color: #333;
  margin-bottom: 20px;
}

.top-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.top-buttons {
  display: flex;
  gap: 10px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
}

.btn-add,
.btn-edit,
.btn-delete,
.btn-submit,
.btn-cancel {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-add {
  background-color: #4CAF50;
  color: white;
}

.btn-add:hover {
  background-color: #45a049;
}

.btn-edit {
  background-color: #2196F3;
  color: white;
  margin-right: 8px;
}

.btn-edit:hover {
  background-color: #0b7dda;
}

.btn-delete {
  background-color: #f44336;
  color: white;
}

.btn-delete:hover {
  background-color: #d32f2f;
}

.btn-delete:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-export {
  background-color: #ff9800;
  color: white;
}

.btn-export:hover {
  background-color: #e68900;
}

.btn-import {
  background-color: #9c27b0;
  color: white;
}

.btn-import:hover {
  background-color: #7b1fa2;
}

.btn-submit {
  background-color: #4CAF50;
  color: white;
}

.btn-submit:hover {
  background-color: #45a049;
}

.btn-cancel {
  background-color: #ccc;
  color: #333;
  margin-right: 10px;
}

.btn-cancel:hover {
  background-color: #999;
}

.table-container {
  overflow-x: auto;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.data-table th {
  background-color: #f2f2f2;
  font-weight: 600;
  color: #333;
}

.data-table tr:hover {
  background-color: #f5f5f5;
}

.action-buttons {
  display: flex;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.pagination button {
  padding: 5px 10px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
}

.pagination button:hover:not(:disabled) {
  background-color: #f5f5f5;
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}
</style>