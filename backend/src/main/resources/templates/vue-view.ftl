<template>
  <div class="${componentName?lower_case}-management-container">
    <div class="${componentName?lower_case}-page-header">
      <h2>${componentName} Management</h2>
    </div>
    <${componentName} />
  </div>
</template>

<script>
import ${componentName} from '../components/${componentName}.vue'

export default {
  name: '${componentName}Management',
  components: {
    ${componentName}
  }
}
</script>

<style scoped>
.${componentName?lower_case}-management-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.${componentName?lower_case}-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.${componentName?lower_case}-page-header h2 {
  margin: 0;
}

.back-button {
  background-color: #95a5a6;
}

.back-button:hover {
  background-color: #7f8c8d;
}
</style>