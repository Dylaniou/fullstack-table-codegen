package com.codegenerator.backend.service;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成服务类，作为协调器，整合数据库元数据、模板渲染和文件生成服务
 */
@Service
public class CodeGeneratorService {

    @Autowired
    private DatabaseMetadataService databaseMetadataService;

    @Autowired
    private TemplateRenderingService templateRenderingService;

    @Autowired
    private FileGeneratorService fileGeneratorService;

    @Value("${codegenerator.frontend.output.path}")
    private String frontendPath;

    @Value("${codegenerator.backend.output.path}")
    private String backendBasePath;
    
    // 各层单独路径配置
    @Value("${codegenerator.backend.model.path}")
    private String backendModelPath;
    
    @Value("${codegenerator.backend.repository.path}")
    private String backendRepositoryPath;
    
    @Value("${codegenerator.backend.controller.path}")
    private String backendControllerPath;
    
    // 前端各目录单独路径配置
    @Value("${codegenerator.frontend.components.path}")
    private String frontendComponentsPath;
    
    @Value("${codegenerator.frontend.views.path}")
    private String frontendViewsPath;
    
    @Value("${codegenerator.package.model}")
    private String modelPackage;
    
    @Value("${codegenerator.package.repository}")
    private String repositoryPackage;
    
    @Value("${codegenerator.package.controller}")
    private String controllerPackage;
    
    @Value("${spring.freemarker.template-loader-path}")
    private String templatesPath;
    
    @Value("${codegenerator.frontend.output.path}/router/index.js")
    private String frontendRouterPath;
    
    @Value("${codegenerator.frontend.output.path}/layouts/MainLayout.vue")
    private String frontendLayoutPath;

    /**
     * 获取指定schema中的所有表名
     * @param schemaName 数据库schema名称
     * @return 表名列表
     * @throws SQLException SQL异常
     */
    public List<String> getAllTables(String schemaName) throws SQLException {
        return databaseMetadataService.getAllTables(schemaName);
    }

    /**
     * 获取指定schema中表的元数据
     * @param schemaName 数据库schema名称
     * @param tableName 表名
     * @return 表元数据
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getTableMetadata(String schemaName, String tableName) throws SQLException {
        return databaseMetadataService.getTableMetadata(schemaName, tableName);
    }

    /**
     * 生成指定schema中表的后端代码
     * @param schemaName 数据库schema名称
     * @param tableName 表名
     * @param outputPath 输出路径（可选，为空则使用配置文件中的路径）
     * @param basePackage 基础包名（可选，为空则使用配置文件中的包名）
     * @param overwrite 是否覆盖已存在的文件
     * @throws SQLException SQL异常
     * @throws IOException IO异常
     * @throws TemplateException 模板异常
     */
    public void generateBackendCode(String schemaName, String tableName, String outputPath, String basePackage, boolean overwrite) throws SQLException, IOException, TemplateException {
        System.out.println("正在为表 '" + schemaName + "." + tableName + "' 生成后端代码...");
        Map<String, Object> data = databaseMetadataService.getTableMetadata(schemaName, tableName);
        String className = (String) data.get("className");
        
        // 确定最终使用的路径和包名
        String finalBackendBasePath = backendBasePath;
        String finalBackendModelPath = backendModelPath;
        String finalBackendRepositoryPath = backendRepositoryPath;
        String finalBackendControllerPath = backendControllerPath;
        String finalModelPackage = modelPackage;
        String finalRepositoryPackage = repositoryPackage;
        String finalControllerPackage = controllerPackage;
        
        // 如果提供了基础包名，计算各个组件的包名
        if (basePackage != null && !basePackage.trim().isEmpty()) {
            finalModelPackage = basePackage + ".model";
            finalRepositoryPackage = basePackage + ".repository";
            finalControllerPackage = basePackage + ".controller";
        }
        
        // 如果提供了输出路径，使用它覆盖所有路径
        if (outputPath != null && !outputPath.trim().isEmpty()) {
            finalBackendBasePath = outputPath;
            
            // 将包名转换为文件路径
            String modelPath = finalModelPackage.replace('.', '/');
            String repositoryPath = finalRepositoryPackage.replace('.', '/');
            String controllerPath = finalControllerPackage.replace('.', '/');
            
            finalBackendModelPath = finalBackendBasePath + "/" + modelPath;
            finalBackendRepositoryPath = finalBackendBasePath + "/" + repositoryPath;
            finalBackendControllerPath = finalBackendBasePath + "/" + controllerPath;
        }
        
        // 更新数据模型中的包名
        data.put("modelPackage", finalModelPackage);
        data.put("repositoryPackage", finalRepositoryPackage);
        data.put("controllerPackage", finalControllerPackage);
        
        // 存储将要生成的文件路径
        List<String> generatedFiles = new ArrayList<>();
        String modelFilePath = finalBackendModelPath + "/" + className + ".java";
        String repositoryFilePath = finalBackendRepositoryPath + "/" + className + "Repository.java";
        String controllerFilePath = finalBackendControllerPath + "/" + className + "Controller.java";
        
        try {
            // 检查文件是否已存在，如果存在且不允许覆盖，则跳过
            if (!overwrite) {
                if (Files.exists(Paths.get(modelFilePath))) {
                    System.out.println("文件已存在且不允许覆盖，跳过生成: " + modelFilePath);
                } else {
                    // 生成Model文件
                    fileGeneratorService.generateJavaFile(data, "model.ftl", modelFilePath, templateRenderingService);
                    // 成功生成后添加到列表中
                    generatedFiles.add(modelFilePath);
                }
                
                if (Files.exists(Paths.get(repositoryFilePath))) {
                    System.out.println("文件已存在且不允许覆盖，跳过生成: " + repositoryFilePath);
                } else {
                    // 生成Repository文件
                    fileGeneratorService.generateJavaFile(data, "repository.ftl", repositoryFilePath, templateRenderingService);
                    // 成功生成后添加到列表中
                    generatedFiles.add(repositoryFilePath);
                }
                
                if (Files.exists(Paths.get(controllerFilePath))) {
                    System.out.println("文件已存在且不允许覆盖，跳过生成: " + controllerFilePath);
                } else {
                    // 生成Controller文件
                    fileGeneratorService.generateJavaFile(data, "controller.ftl", controllerFilePath, templateRenderingService);
                    // 成功生成后添加到列表中
                    generatedFiles.add(controllerFilePath);
                }
            } else {
                // 覆盖模式下，直接生成文件
                // 生成Model文件
                fileGeneratorService.generateJavaFile(data, "model.ftl", modelFilePath, templateRenderingService);
                generatedFiles.add(modelFilePath);
                
                // 生成Repository文件
                fileGeneratorService.generateJavaFile(data, "repository.ftl", repositoryFilePath, templateRenderingService);
                generatedFiles.add(repositoryFilePath);
                
                // 生成Controller文件
                fileGeneratorService.generateJavaFile(data, "controller.ftl", controllerFilePath, templateRenderingService);
                generatedFiles.add(controllerFilePath);
            }
            
            System.out.println("后端代码生成成功，文件路径：" + generatedFiles);
        } catch (Exception e) {
            System.err.println("后端代码生成失败：" + e.getMessage());
            // 删除已生成的文件
            fileGeneratorService.deleteFilesIfExists(generatedFiles);
            // 重新抛出异常，让上层处理
            if (e instanceof SQLException) throw (SQLException)e;
            if (e instanceof IOException) throw (IOException)e;
            if (e instanceof TemplateException) throw (TemplateException)e;
            throw new RuntimeException("生成后端代码时发生未知错误", e);
        }
    }

    /**
     * 生成指定schema中表的前端代码
     * @param schemaName 数据库schema名称
     * @param tableName 表名
     * @param outputPath 输出路径（可选，为空则使用配置文件中的路径）
     * @param overwrite 是否覆盖已存在的文件
     * @throws SQLException SQL异常
     * @throws IOException IO异常
     * @throws TemplateException 模板异常
     */
    public void generateFrontendCode(String schemaName, String tableName, String outputPath, boolean overwrite) throws SQLException, IOException, TemplateException {
        System.out.println("正在为表 '" + schemaName + "." + tableName + "' 生成前端代码...");
        Map<String, Object> data = databaseMetadataService.getTableMetadata(schemaName, tableName);
        String componentName = (String) data.get("componentName");
        
        // 确定最终使用的路径
        String finalFrontendPath = frontendPath;
        String finalFrontendComponentsPath = frontendComponentsPath;
        String finalFrontendViewsPath = frontendViewsPath;
        String finalFrontendRouterPath = frontendRouterPath;
        String finalFrontendLayoutPath = frontendLayoutPath;
        
        // 如果提供了输出路径，使用它覆盖所有路径
        if (outputPath != null && !outputPath.trim().isEmpty()) {
            finalFrontendPath = outputPath;
            finalFrontendComponentsPath = outputPath;
            finalFrontendViewsPath = outputPath.replace("components", "views");
            finalFrontendRouterPath = outputPath + "/router/index.js";
            finalFrontendLayoutPath = outputPath + "/layouts/MainLayout.vue";
        }
        
        // 存储将要生成的文件路径
        List<String> generatedFiles = new ArrayList<>();
        String componentFilePath = finalFrontendComponentsPath + "/" + componentName + ".vue";
        String viewFilePath = finalFrontendViewsPath + "/" + componentName + "Management.vue";
        
        generatedFiles.add(componentFilePath);
        generatedFiles.add(viewFilePath);
        
        // 存储需要备份的文件路径
        Map<String, String> backedUpFiles = new HashMap<>();
        
        try {
            // 检查文件是否已存在，如果存在且不允许覆盖，则跳过
            if (!overwrite) {
                for (String filePath : generatedFiles) {
                    if (Files.exists(Paths.get(filePath))) {
                        System.out.println("文件已存在且不允许覆盖，跳过生成: " + filePath);
                        continue;
                    }
                }
            }
            
            // 为路由配置文件和布局文件创建临时备份
            if (Files.exists(Paths.get(finalFrontendRouterPath))) {
                String routerBackupPath = finalFrontendRouterPath + ".backup." + System.currentTimeMillis();
                Files.copy(Paths.get(finalFrontendRouterPath), Paths.get(routerBackupPath), StandardCopyOption.REPLACE_EXISTING);
                backedUpFiles.put(finalFrontendRouterPath, routerBackupPath);
            }
            
            if (Files.exists(Paths.get(finalFrontendLayoutPath))) {
                String layoutBackupPath = finalFrontendLayoutPath + ".backup." + System.currentTimeMillis();
                Files.copy(Paths.get(finalFrontendLayoutPath), Paths.get(layoutBackupPath), StandardCopyOption.REPLACE_EXISTING);
                backedUpFiles.put(finalFrontendLayoutPath, layoutBackupPath);
            }
            
            // 生成Vue组件
            fileGeneratorService.generateFrontendFile(data, "vue-component.ftl", componentFilePath, templateRenderingService);
            
            // 生成Vue视图
            fileGeneratorService.generateFrontendFile(data, "vue-view.ftl", viewFilePath, templateRenderingService);
            
            // 更新路由配置文件
            updateRouterConfiguration(componentName, finalFrontendRouterPath);
            
            // 更新布局文件中的导航链接
            updateLayoutNavigation(componentName, finalFrontendLayoutPath);
            
            System.out.println("前端代码生成成功，文件路径：" + generatedFiles);
        } catch (Exception e) {
            System.err.println("前端代码生成失败：" + e.getMessage());
            // 删除已生成的文件
            fileGeneratorService.deleteFilesIfExists(generatedFiles);
            
            // 恢复被修改的文件
            for (Map.Entry<String, String> entry : backedUpFiles.entrySet()) {
                String originalFilePath = entry.getKey();
                String backupFilePath = entry.getValue();
                try {
                    if (Files.exists(Paths.get(backupFilePath))) {
                        Files.copy(Paths.get(backupFilePath), Paths.get(originalFilePath), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("已恢复文件：" + originalFilePath);
                    }
                } catch (IOException ioException) {
                    System.err.println("恢复文件失败：" + originalFilePath + ", 错误：" + ioException.getMessage());
                }
            }
            
            // 重新抛出异常，让上层处理
            if (e instanceof SQLException) throw (SQLException)e;
            if (e instanceof IOException) throw (IOException)e;
            if (e instanceof TemplateException) throw (TemplateException)e;
            throw new RuntimeException("生成前端代码时发生未知错误", e);
        } finally {
            // 清理备份文件
            for (String backupFilePath : backedUpFiles.values()) {
                try {
                    Files.deleteIfExists(Paths.get(backupFilePath));
                } catch (IOException ioException) {
                    System.err.println("清理备份文件失败：" + backupFilePath + ", 错误：" + ioException.getMessage());
                }
            }
        }
    }
    
    /**
     * 生成指定表的全栈代码（前后端）
     * @param schemaName 数据库schema名称
     * @param tableName 表名
     * @param outputPath 输出路径（可选，为空则使用配置文件中的路径）
     * @param basePackage 基础包名（可选，为空则使用配置文件中的包名）
     * @param overwrite 是否覆盖已存在的文件
     * @throws SQLException SQL异常
     * @throws IOException IO异常
     * @throws TemplateException 模板异常
     */
    public void generateFullstackCode(String schemaName, String tableName, String outputPath, String basePackage, boolean overwrite) throws SQLException, IOException, TemplateException {
        System.out.println("正在为表 '" + schemaName + "." + tableName + "' 生成全栈代码...");
        
        // 用于存储已生成的后端文件路径，用于回滚
        List<String> generatedBackendFiles = new ArrayList<>();
        
        try {
            // 1. 获取表元数据
            Map<String, Object> data = databaseMetadataService.getTableMetadata(schemaName, tableName);
            String className = (String) data.get("className");
            
            // 2. 确定后端文件路径
            String finalBackendBasePath = backendBasePath;
            String finalBackendModelPath = backendModelPath;
            String finalBackendRepositoryPath = backendRepositoryPath;
            String finalBackendControllerPath = backendControllerPath;
            
            // 如果提供了输出路径，使用它覆盖所有路径
            if (outputPath != null && !outputPath.trim().isEmpty()) {
                finalBackendBasePath = outputPath;
                
                // 将包名转换为文件路径
                String modelPath = modelPackage.replace('.', '/');
                String repositoryPath = repositoryPackage.replace('.', '/');
                String controllerPath = controllerPackage.replace('.', '/');
                
                finalBackendModelPath = finalBackendBasePath + "/" + modelPath;
                finalBackendRepositoryPath = finalBackendBasePath + "/" + repositoryPath;
                finalBackendControllerPath = finalBackendBasePath + "/" + controllerPath;
            }
            
            // 3. 调用现有的后端代码生成方法
            generateBackendCode(schemaName, tableName, outputPath, basePackage, overwrite);
            
            // 4. 记录已生成的后端文件路径
            String modelFilePath = finalBackendModelPath + "/" + className + ".java";
            String repositoryFilePath = finalBackendRepositoryPath + "/" + className + "Repository.java";
            String controllerFilePath = finalBackendControllerPath + "/" + className + "Controller.java";
            
            if (Files.exists(Paths.get(modelFilePath))) {
                generatedBackendFiles.add(modelFilePath);
            }
            if (Files.exists(Paths.get(repositoryFilePath))) {
                generatedBackendFiles.add(repositoryFilePath);
            }
            if (Files.exists(Paths.get(controllerFilePath))) {
                generatedBackendFiles.add(controllerFilePath);
            }
            
            // 5. 调用现有的前端代码生成方法
            generateFrontendCode(schemaName, tableName, outputPath, overwrite);
            
            System.out.println("全栈代码生成成功，表：" + schemaName + "." + tableName);
        } catch (Exception e) {
            System.err.println("全栈代码生成失败：" + e.getMessage());
            
            // 回滚已生成的后端代码
            if (!generatedBackendFiles.isEmpty()) {
                fileGeneratorService.deleteFilesIfExists(generatedBackendFiles);
            }
            
            // 重新抛出异常，让上层处理
            if (e instanceof SQLException) throw (SQLException)e;
            if (e instanceof IOException) throw (IOException)e;
            if (e instanceof TemplateException) throw (TemplateException)e;
            throw new RuntimeException("生成全栈代码时发生未知错误", e);
        }
    }
    
    /**
     * 批量生成代码
     * @param schemaName 数据库schema名称
     * @param tableNames 表名列表
     * @param outputPath 输出路径（可选，为空则使用配置文件中的路径）
     * @param basePackage 基础包名（可选，为空则使用配置文件中的包名）
     * @param overwrite 是否覆盖已存在的文件
     * @throws Exception 异常
     */
    public void batchGenerateCode(String schemaName, List<String> tableNames, String outputPath, String basePackage, boolean overwrite) throws Exception {
        List<String> failedTables = new ArrayList<>();
        
        for (String tableName : tableNames) {
            try {
                // 使用新的全栈代码生成方法，确保单个表的前后端代码生成具有原子性
                generateFullstackCode(schemaName, tableName, outputPath, basePackage, overwrite);
            } catch (Exception e) {
                System.err.println("为表 '" + schemaName + "." + tableName + "' 生成代码失败：" + e.getMessage());
                failedTables.add(tableName);
            }
        }
        
        if (!failedTables.isEmpty()) {
            System.err.println("以下表生成代码失败：" + failedTables);
            throw new RuntimeException("部分表生成代码失败，请查看日志获取详细信息");
        }
    }
    
    /**
     * 删除生成的代码
     * @param schemaName 数据库schema名称
     * @param tableName 表名
     * @param deleteType 删除类型：backend/frontend/fullstack
     * @param outputPath 输出路径（可选，为空则使用配置文件中的路径）
     * @throws SQLException SQL异常
     * @throws IOException IO异常
     */
    public void deleteGeneratedCode(String schemaName, String tableName, String deleteType, String outputPath) throws SQLException, IOException {
        System.out.println("正在删除表 '" + schemaName + "." + tableName + "' 的" + deleteType + "代码...");
        
        // 获取表元数据
        Map<String, Object> data = databaseMetadataService.getTableMetadata(schemaName, tableName);
        String className = (String) data.get("className");
        String componentName = (String) data.get("componentName");
        
        // 存储要删除的文件路径
        List<String> filesToDelete = new ArrayList<>();
        
        // 确定最终使用的路径
        String finalBackendBasePath = backendBasePath;
        String finalBackendModelPath = backendModelPath;
        String finalBackendRepositoryPath = backendRepositoryPath;
        String finalBackendControllerPath = backendControllerPath;
        String finalFrontendPath = frontendPath;
        String finalFrontendComponentsPath = frontendComponentsPath;
        String finalFrontendViewsPath = frontendViewsPath;
        String finalFrontendRouterPath = frontendRouterPath;
        String finalFrontendLayoutPath = frontendLayoutPath;
        
        // 如果提供了输出路径，使用它覆盖所有路径
        if (outputPath != null && !outputPath.trim().isEmpty()) {
            finalBackendBasePath = outputPath;
            finalFrontendPath = outputPath;
            
            // 后端路径
            String modelPath = modelPackage.replace('.', '/');
            String repositoryPath = repositoryPackage.replace('.', '/');
            String controllerPath = controllerPackage.replace('.', '/');
            
            finalBackendModelPath = finalBackendBasePath + "/" + modelPath;
            finalBackendRepositoryPath = finalBackendBasePath + "/" + repositoryPath;
            finalBackendControllerPath = finalBackendBasePath + "/" + controllerPath;
            
            // 前端路径
            finalFrontendComponentsPath = outputPath;
            finalFrontendViewsPath = outputPath.replace("components", "views");
            finalFrontendRouterPath = outputPath + "/router/index.js";
            finalFrontendLayoutPath = outputPath + "/layouts/MainLayout.vue";
        }
        
        // 根据删除类型添加要删除的文件
        if ("backend".equals(deleteType) || "fullstack".equals(deleteType)) {
            // 后端文件
            filesToDelete.add(finalBackendModelPath + "/" + className + ".java");
            filesToDelete.add(finalBackendRepositoryPath + "/" + className + "Repository.java");
            filesToDelete.add(finalBackendControllerPath + "/" + className + "Controller.java");
        }
        
        if ("frontend".equals(deleteType) || "fullstack".equals(deleteType)) {
            // 前端文件
            filesToDelete.add(finalFrontendComponentsPath + "/" + componentName + ".vue");
            filesToDelete.add(finalFrontendViewsPath + "/" + componentName + "Management.vue");
            
            // 为路由配置文件和布局文件创建临时备份
            Map<String, String> backedUpFiles = new HashMap<>();
            
            if (Files.exists(Paths.get(finalFrontendRouterPath))) {
                String routerBackupPath = finalFrontendRouterPath + ".backup." + System.currentTimeMillis();
                Files.copy(Paths.get(finalFrontendRouterPath), Paths.get(routerBackupPath), StandardCopyOption.REPLACE_EXISTING);
                backedUpFiles.put(finalFrontendRouterPath, routerBackupPath);
            }
            
            if (Files.exists(Paths.get(finalFrontendLayoutPath))) {
                String layoutBackupPath = finalFrontendLayoutPath + ".backup." + System.currentTimeMillis();
                Files.copy(Paths.get(finalFrontendLayoutPath), Paths.get(layoutBackupPath), StandardCopyOption.REPLACE_EXISTING);
                backedUpFiles.put(finalFrontendLayoutPath, layoutBackupPath);
            }
            
            try {
                // 从路由配置中移除
                removeFromRouterConfiguration(componentName, finalFrontendRouterPath);
                
                // 从布局文件的导航中移除
                removeFromLayoutNavigation(componentName, finalFrontendLayoutPath);
            } catch (Exception e) {
                System.err.println("更新前端配置失败：" + e.getMessage());
                
                // 恢复被修改的文件
                for (Map.Entry<String, String> entry : backedUpFiles.entrySet()) {
                    String originalFilePath = entry.getKey();
                    String backupFilePath = entry.getValue();
                    try {
                        if (Files.exists(Paths.get(backupFilePath))) {
                            Files.copy(Paths.get(backupFilePath), Paths.get(originalFilePath), StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("已恢复文件：" + originalFilePath);
                        }
                    } catch (IOException ioException) {
                        System.err.println("恢复文件失败：" + originalFilePath + ", 错误：" + ioException.getMessage());
                    }
                }
                throw e;
            } finally {
                // 清理备份文件
                for (String backupFilePath : backedUpFiles.values()) {
                    try {
                        Files.deleteIfExists(Paths.get(backupFilePath));
                    } catch (IOException ioException) {
                        System.err.println("清理备份文件失败：" + backupFilePath + ", 错误：" + ioException.getMessage());
                    }
                }
            }
        }
        
        // 删除文件
        fileGeneratorService.deleteFilesIfExists(filesToDelete);
        
        System.out.println("代码删除成功，表：" + schemaName + "." + tableName);
    }
    
    /**
     * 从路由配置文件中移除组件配置
     * @param componentName 组件名称
     * @param routerFilePath 路由文件路径
     * @throws IOException IO异常
     */
    private void removeFromRouterConfiguration(String componentName, String routerFilePath) throws IOException {
        if (!Files.exists(Paths.get(routerFilePath))) {
            System.out.println("路由配置文件不存在: " + routerFilePath);
            return;
        }
        
        // 读取现有的路由配置文件内容
        List<String> lines = Files.readAllLines(Paths.get(routerFilePath));
        
        // 要删除的导入语句（使用懒加载格式）
        String importStatement = "const " + componentName + "Management = () => import('../views/" + componentName + "Management.vue')";
        
        // 要删除的路由路径
        String routePath = "/" + componentName.toLowerCase();
        
        // 查找并移除导入语句
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(importStatement)) {
                lines.remove(i);
                // 如果下一行是空行，也移除
                if (i < lines.size() && lines.get(i).trim().isEmpty()) {
                    lines.remove(i);
                }
                break;
            }
        }
        
        // 查找并移除路由配置
        int routesStartIndex = -1;
        int routesEndIndex = -1;
        int routeStartIndex = -1;
        int routeEndIndex = -1;
        
        // 找到routes数组的范围
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals("const routes = [")) {
                routesStartIndex = i;
            }
            if (routesStartIndex >= 0 && lines.get(i).trim().equals("]")) {
                routesEndIndex = i;
                break;
            }
        }
        
        // 找到要删除的路由配置的范围
        if (routesStartIndex >= 0 && routesEndIndex >= 0) {
            boolean inRouteBlock = false;
            for (int i = routesStartIndex; i < routesEndIndex; i++) {
                String line = lines.get(i).trim();
                if (line.startsWith("path:") && line.contains(routePath)) {
                    inRouteBlock = true;
                    routeStartIndex = i;
                    // 向前查找开始位置（大括号）
                    for (int j = i; j >= routesStartIndex; j--) {
                        if (lines.get(j).trim().equals("{")) {
                            routeStartIndex = j;
                            break;
                        }
                    }
                }
                if (inRouteBlock && line.equals("}")) {
                    routeEndIndex = i + 1;
                    // 检查下一行是否是逗号，如果是也移除
                    if (routeEndIndex < lines.size() && lines.get(routeEndIndex).trim().equals(",")) {
                        routeEndIndex++;
                    }
                    break;
                }
            }
            
            // 如果找到了要删除的路由配置块，则移除
            if (routeStartIndex >= 0 && routeEndIndex > routeStartIndex) {
                lines.subList(routeStartIndex, routeEndIndex).clear();
            }
        }
        
        // 写回文件
        Files.write(Paths.get(routerFilePath), lines);
        System.out.println("已从路由配置中移除: " + componentName);
    }
    
    /**
     * 从布局文件中移除导航链接
     * @param componentName 组件名称
     * @param layoutFilePath 布局文件路径
     * @throws IOException IO异常
     */
    private void removeFromLayoutNavigation(String componentName, String layoutFilePath) throws IOException {
        if (!Files.exists(Paths.get(layoutFilePath))) {
            System.out.println("布局文件不存在: " + layoutFilePath);
            return;
        }
        
        // 读取现有的布局文件内容
        String layoutContent = new String(Files.readAllBytes(Paths.get(layoutFilePath)));
        
        // 要查找的路由路径
        String routePath = "/" + componentName.toLowerCase();
        
        // 查找包含该路由路径的菜单项
        int menuItemStartIndex = layoutContent.indexOf("<el-menu-item index=\"" + routePath + "\">");
        if (menuItemStartIndex == -1) {
            System.out.println("未找到菜单项: " + routePath);
            return;
        }
        
        // 查找菜单项的结束位置
        int menuItemEndIndex = layoutContent.indexOf("</el-menu-item>", menuItemStartIndex);
        if (menuItemEndIndex == -1) {
            System.out.println("未找到菜单项结束标签");
            return;
        }
        
        // 包含结束标签的完整长度
        menuItemEndIndex += "</el-menu-item>".length();
        
        // 如果结束标签后面有换行符，也包含在内
        if (menuItemEndIndex < layoutContent.length() && layoutContent.charAt(menuItemEndIndex) == '\n') {
            menuItemEndIndex++;
        }
        
        // 构建新的布局内容
        StringBuilder sb = new StringBuilder();
        sb.append(layoutContent.substring(0, menuItemStartIndex));
        sb.append(layoutContent.substring(menuItemEndIndex));
        
        // 写回文件
        Files.write(Paths.get(layoutFilePath), sb.toString().getBytes());
        System.out.println("已从导航菜单中移除: " + componentName);
    }

    /**
     * 更新路由配置文件
     * @param componentName 组件名称
     * @param routerFilePath 路由文件路径
     * @throws IOException IO异常
     */
    private void updateRouterConfiguration(String componentName, String routerFilePath) throws IOException {
        if (!Files.exists(Paths.get(routerFilePath))) {
            System.out.println("路由配置文件不存在: " + routerFilePath);
            return;
        }
        
        // 读取现有的路由配置文件内容
        List<String> lines = Files.readAllLines(Paths.get(routerFilePath));
        
        // 检查是否已存在该组件的导入语句（使用懒加载格式）
        String importStatement = "const " + componentName + "Management = () => import('../views/" + componentName + "Management.vue')";
        boolean importExists = false;
        int importLineIndex = -1;
        
        // 查找导入部分的最后一行（兼容普通导入和懒加载导入）
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(importStatement)) {
                importExists = true;
                break;
            }
            if ((lines.get(i).startsWith("import ") && !lines.get(i).startsWith("import {")) || 
                lines.get(i).startsWith("const ") && lines.get(i).contains("=> import")) {
                importLineIndex = i;
            }
        }
        
        // 如果不存在导入语句，则添加
        if (!importExists && importLineIndex >= 0) {
            lines.add(importLineIndex + 1, importStatement);
        }
        
        // 检查是否已存在该组件的路由配置
        String routePath = "/" + componentName.toLowerCase();
        boolean routeExists = false;
        
        for (String line : lines) {
            if (line.trim().startsWith("path:") && line.contains(routePath)) {
                routeExists = true;
                break;
            }
        }
        
        // 如果不存在路由配置，则添加
        if (!routeExists) {
            // 查找routes数组的位置
            int routesStartIndex = -1;
            int routesEndIndex = -1;
            
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("const routes = [")) {
                    routesStartIndex = i;
                }
                if (routesStartIndex >= 0 && lines.get(i).trim().equals("]")) {
                    routesEndIndex = i;
                    break;
                }
            }
            
            if (routesStartIndex >= 0 && routesEndIndex >= 0) {
                // 构建新的路由配置
                String newRoute = "  {\n" +
                                 "    path: '" + routePath + "',\n" +
                                 "    name: '" + componentName + "Management',\n" +
                                 "    component: " + componentName + "Management\n" +
                                 "  }";
                
                // 检查routes数组是否为空，或者最后一个元素是否以逗号结尾
                boolean needComma = false;
                if (routesEndIndex > routesStartIndex + 1) {
                    // 如果routes数组中已经有元素，检查最后一个元素是否以逗号结尾
                    String lastRouteLine = lines.get(routesEndIndex - 1).trim();
                    if (!lastRouteLine.endsWith(",")) {
                        needComma = true;
                    }
                }
                
                // 在routes数组末尾添加新路由配置（在]之前）
                lines.add(routesEndIndex, newRoute);
                
                // 如果需要逗号，在新路由配置之前添加逗号
                if (needComma) {
                    lines.add(routesEndIndex, "  ,");
                }
            }
        }
        
        // 写回文件
        Files.write(Paths.get(routerFilePath), lines);
        System.out.println("路由配置文件已更新: " + routerFilePath);
    }
    
    /**
     * 更新布局文件中的导航链接
     * @param componentName 组件名称
     * @param layoutFilePath 布局文件路径
     * @throws IOException IO异常
     */
    private void updateLayoutNavigation(String componentName, String layoutFilePath) throws IOException {
        if (!Files.exists(Paths.get(layoutFilePath))) {
            System.out.println("布局文件不存在: " + layoutFilePath);
            return;
        }
        
        // 读取现有的布局文件内容
        String layoutContent = new String(Files.readAllBytes(Paths.get(layoutFilePath)));
        
        // 检查导航链接是否已存在
        String routePath = "/" + componentName.toLowerCase();
        String navText = componentName + " Management";
        if (layoutContent.contains("index=\"" + routePath + "\"")) {
            System.out.println("导航链接已存在: " + routePath);
            return;
        }
        
        // 查找el-menu的结束标签（带有正确缩进的）
        // 我们要找的是带有两个Tab（8个空格）缩进的</el-menu>标签
        String menuEndTag = "        </el-menu>";
        int menuEndIndex = layoutContent.indexOf(menuEndTag);
        
        // 如果找不到带有特定缩进的结束标签，则使用常规查找
        if (menuEndIndex == -1) {
            menuEndIndex = layoutContent.lastIndexOf("</el-menu>");
            if (menuEndIndex == -1) {
                System.out.println("未找到el-menu的结束标签");
                return;
            }
        }
        
        // 构建新的菜单项，确保与现有菜单项有相同的缩进格式
        // 现有菜单项格式: 10个空格（两个Tab + 两个空格）+ <el-menu-item index="...">
        //                 12个空格（两个Tab + 四个空格）+ <el-icon>...</el-icon>
        //                 12个空格（两个Tab + 四个空格）+ <span>...</span>
        //                 10个空格（两个Tab + 两个空格）+ </el-menu-item>
        String newMenuItem = "          <el-menu-item index=\"" + routePath + "\">\n" +
                           "            <el-icon><Code /></el-icon>\n" +
                           "            <span>" + navText + "</span>\n" +
                           "          </el-menu-item>\n";
        
        // 在el-menu结束标签前插入新菜单项
        StringBuilder sb = new StringBuilder(layoutContent);
        sb.insert(menuEndIndex, newMenuItem);
        
        // 确保el-menu结束标签有正确的缩进（两个Tab）
        if (menuEndIndex > 0 && menuEndTag.length() > 0 && !layoutContent.substring(menuEndIndex).startsWith(menuEndTag)) {
            // 查找并替换结束标签为带有正确缩进的版本
            int originalEndTagIndex = layoutContent.lastIndexOf("</el-menu>");
            if (originalEndTagIndex > 0) {
                int endTagEndIndex = originalEndTagIndex + "</el-menu>".length();
                sb.replace(originalEndTagIndex, endTagEndIndex, menuEndTag);
            }
        }
        
        layoutContent = sb.toString();
        
        // 写回文件
        Files.write(Paths.get(layoutFilePath), layoutContent.getBytes());
        System.out.println("已添加新菜单项: " + navText + "，路径: " + routePath);
        System.out.println("布局文件已更新: " + layoutFilePath);
    }
}