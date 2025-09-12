<template>
  <div class="user-management-container">
    <div class="user-page-header">
      <h2>User Management</h2>
    </div>
    <User />
  </div>
</template>

<script>
import User from '../components/User.vue'

export default {
  name: 'UserManagement',
  components: {
    User
  }
}
</script>

<style scoped>
.user-management-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.user-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.user-page-header h2 {
  margin: 0;
}

.back-button {
  background-color: #95a5a6;
}

.back-button:hover {
  background-color: #7f8c8d;
}
</style>