# fullstack-table-codegen
A complete automatic code generation system/tool: it generates full-stack (frontend &amp; backend) code based on database table fields/structures, boosts development efficiency. It has two parts – frontend (user-friendly interface) and backend (core code generation logic).
## 项目概述

这是一个完整的代码自动生成系统，能够根据数据库表结构自动生成前后端代码，提高开发效率。系统分为前端和后端两个部分，前端提供用户友好的界面，后端负责核心的代码生成逻辑。

## 项目结构

```
0906/
├── backend/             # Spring Boot后端服务
├── frontend/            # Vue 3前端应用
├── sql/                 # 数据库脚本文件
└── 优化改进记录.md       # 项目优化和改进记录
```

## 技术栈

### 后端技术栈
- Java 8+
- Spring Boot 2.7.x
- Spring Data JPA
- FreeMarker模板引擎
- MySQL数据库
- Swagger API文档
- Spring Boot DevTools（热部署）

### 前端技术栈
- Vue 3
- Vue Router 4
- Element Plus
- Axios
- SCSS

## 核心功能

### 后端核心功能
1. **数据库元数据获取**：通过`DatabaseMetadataService`获取数据库表结构信息
2. **代码模板渲染**：通过`TemplateRenderingService`使用FreeMarker渲染代码模板
3. **文件生成**：通过`FileGeneratorService`将渲染后的代码写入文件
4. **代码生成协调**：通过`CodeGeneratorService`协调各服务完成代码生成
5. **REST API接口**：通过`CodeGeneratorController`提供HTTP接口
6. **热部署支持**：通过Spring Boot DevTools实现代码变动实时识别与自动重启

### 前端核心功能
1. **数据库表列表展示**：通过`TableList`组件展示数据库中的表列表
2. **代码生成配置**：通过`CodeGenerator`组件提供代码生成的各种配置选项
3. **表结构预览**：支持预览数据库表的结构信息
4. **代码生成结果展示**：展示代码生成的结果和生成的文件列表
5. **代码删除功能**：在数据库表界面支持删除已生成的代码
6. **响应式设计**：适配不同屏幕尺寸的设备

## 快速开始

### 后端部署

1. 进入后端目录
```bash
cd backend
```

2. 修改配置文件
   - 编辑`src/main/resources/application.properties`
   - 配置数据库连接信息、代码输出路径、包名等

3. 构建和运行
```bash
# 使用Maven构建
mvn clean package

# 运行应用
java -jar target/backend-0.0.1-SNAPSHOT.jar

# 或者直接运行主类
# 运行CodeGeneratorApplication.java
```

### 前端部署

1. 进入前端目录
```bash
cd frontend
```

2. 安装依赖
```bash
npm install
```

3. 开发模式运行
```bash
npm run serve
```

4. 构建生产版本
```bash
npm run build
```

## 使用指南

### 1. 访问应用

- 前端应用：默认访问 http://localhost:8081
- 后端API：默认访问 http://localhost:8080
- Swagger文档：http://localhost:8080/swagger-ui.html

### 2. 生成代码流程

1. 在前端界面输入数据库Schema名称，点击"刷新列表"按钮获取数据表列表
2. 在数据表列表中，点击对应表的"生成代码"按钮
3. 在弹出的代码生成选项对话框中，选择生成类型（仅后端代码/仅前端代码/全栈代码）
4. 可选：指定代码输出路径和包名（不填则使用配置文件中的默认值）
5. 点击"确认生成"按钮，系统将开始生成代码
6. 生成完成后，系统将显示生成结果和生成的文件列表

### 3. 删除生成的代码

在数据表列表中，点击对应表的"删除代码"按钮，系统将删除该表生成的代码文件。

## 生成的代码结构

### 后端代码结构
- 实体类（Model）：表示数据库表的Java对象
- 数据访问层（Repository）：提供数据库操作接口
- 服务层（Service）：包含业务逻辑
- 控制器（Controller）：处理HTTP请求，提供REST API
- 异常处理：统一的异常处理机制

### 前端代码结构
- Vue组件：包含表格、表单等UI组件
- 路由配置：定义前端页面路由
- 导航链接：侧边栏导航配置
- API请求：与后端交互的Axios配置

## 热部署功能

项目已配置Spring Boot DevTools实现后端代码热部署功能，具体配置如下：

1. 在`pom.xml`中添加了Spring Boot DevTools依赖
2. 在`application.properties`中配置了热部署相关参数
3. 创建了`.mvn/jvm.config`文件添加JVM参数

**IDE设置**：
- IntelliJ IDEA：开启自动编译功能（Settings → Build, Execution, Deployment → Compiler → 勾选Build project automatically）
- Eclipse：开启自动编译功能（Project → Build Automatically）

**注意**：配置文件和模板文件的变更仍需手动重启应用。

## API接口文档

系统提供了以下主要API接口：

### 数据库操作
- **获取数据库表列表**：`GET /api/codegen/tables/{schemaName}`
- **获取表元数据**：`GET /api/codegen/metadata/{schemaName}/{tableName}`

### 代码生成
- **生成后端代码**：`POST /api/codegen/generate/backend/{schemaName}/{tableName}`
- **生成前端代码**：`POST /api/codegen/generate/frontend/{schemaName}/{tableName}`
- **生成全栈代码**：`POST /api/codegen/generate/fullstack/{schemaName}/{tableName}`
- **批量生成代码**：`POST /api/codegen/generate/batch/{schemaName}`

### 代码删除
- **删除生成的代码**：`DELETE /api/codegen/delete/{schemaName}/{tableName}`

详细的API文档可通过Swagger界面查看。

## 常见问题

### 1. 数据库连接失败
- 检查`application.properties`中的数据库连接信息是否正确
- 确保数据库服务已启动且网络连接正常

### 2. 代码生成失败
- 检查数据库表结构是否符合要求
- 确保指定的输出路径有写入权限
- 查看日志文件了解具体错误信息

### 3. 热部署不生效
- 确认IDE的自动编译功能已开启
- 检查`.mvn/jvm.config`文件是否正确配置
- 尝试手动编译项目触发热部署

## 维护与更新

项目改进记录详见`优化改进记录.md`文件，包含了项目开发过程中的优化点和改进内容。

## 总结

本项目通过自动化代码生成，大幅提高了开发效率，特别是在开发CRUD类型的应用时。系统具有良好的扩展性，可以根据需要自定义代码模板，支持多种数据库类型和前端框架。热部署功能的添加进一步提升了开发体验，使开发者无需频繁重启应用即可看到代码变更的效果。
