# 代码生成器后端服务

这是一个基于Spring Boot的代码生成器后端服务，用于根据数据库表结构自动生成前后端代码。

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/codegenerator/backend/
│   │   │   ├── CodeGeneratorApplication.java  # 应用程序入口
│   │   │   ├── controller/                    # 控制器层
│   │   │   ├── service/                       # 服务层
│   │   │   └── exception/                     # 异常处理
│   │   └── resources/
│   │       ├── application.properties        # 应用程序配置
│   │       └── templates/                     # 代码生成模板
│   └── test/                                 # 测试代码
└── pom.xml                                   # Maven依赖配置
```

## 核心功能

1. **数据库元数据获取**：通过`DatabaseMetadataService`获取数据库表结构信息
2. **代码模板渲染**：通过`TemplateRenderingService`使用FreeMarker渲染代码模板
3. **文件生成**：通过`FileGeneratorService`将渲染后的代码写入文件
4. **代码生成协调**：通过`CodeGeneratorService`协调各服务完成代码生成
5. **REST API接口**：通过`CodeGeneratorController`提供HTTP接口

## 技术栈

- Java 8+
- Spring Boot 2.7.x
- Spring Data JPA
- FreeMarker模板引擎
- MySQL数据库
- Swagger API文档

## 配置说明

主要配置文件位于`src/main/resources/application.properties`，包含以下关键配置：

- 数据库连接信息
- 代码输出路径
- 包名配置
- 模板引擎配置

## 使用方法

1. 修改`application.properties`中的数据库连接信息
2. 运行`CodeGeneratorApplication`启动应用程序
3. 通过前端界面或直接调用API接口生成代码

## API接口

### 获取数据库表列表
```
GET /api/codegen/tables/{schemaName}
```

### 获取表元数据
```
GET /api/codegen/metadata/{schemaName}/{tableName}
```

### 生成后端代码
```
POST /api/codegen/generate/backend/{schemaName}/{tableName}
```

### 生成前端代码
```
POST /api/codegen/generate/frontend/{schemaName}/{tableName}
```

### 生成全栈代码
```
POST /api/codegen/generate/fullstack/{schemaName}/{tableName}
```

### 批量生成代码
```
POST /api/codegen/generate/batch/{schemaName}
```

## 生成的代码结构

### 后端代码
- 实体类（Model）
- 数据访问层（Repository）
- 控制器（Controller）

### 前端代码
- Vue组件
- 路由配置
- 导航链接

## 热部署功能

该项目已配置Spring Boot DevTools实现代码热部署功能，当修改后端代码后不需要重新启动项目即可看到变更效果。

### 配置内容

1. 添加了Spring Boot DevTools依赖到pom.xml
2. 在application.properties中配置了自动重启参数
3. 创建了.mvn/jvm.config文件设置JVM参数

### IDE设置指南

为了确保热部署功能正常工作，请在您的IDE中进行以下设置：

#### IntelliJ IDEA
1. 打开设置（File -> Settings）
2. 导航到Build, Execution, Deployment -> Compiler
3. 勾选"Build project automatically"
4. 按下Shift+Ctrl+Alt+/，选择Registry
5. 勾选"compiler.automake.allow.when.app.running"

#### Eclipse
1. 打开设置（Window -> Preferences）
2. 导航到General -> Workspace
3. 勾选"Refresh using native hooks or polling"
4. 勾选"Build automatically"

### 使用方法

1. 修改Java代码（如Controller、Service等）
2. 保存文件（IntelliJ IDEA会自动编译）
3. 等待1-2秒，Spring Boot DevTools会自动重启应用（只重启变化的部分）
4. 刷新浏览器查看变更效果

注意：配置文件（application.properties）和模板文件的变更可能需要手动重启应用。