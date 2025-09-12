package com.codegenerator.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ControllerTemplateTest {

    @Autowired
    private DatabaseMetadataService databaseMetadataService;

    @Autowired
    private TemplateRenderingService templateRenderingService;

    @Autowired
    private FileGeneratorService fileGeneratorService;

    @Autowired
    private CodeGeneratorService codeGeneratorService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testGeneratedControllerWithAllFeatures() throws Exception {
        // 1. 准备测试数据 - 使用已有的表结构
        String schemaName = "TEST";
        String tableName = "user";
        String outputPath = System.getProperty("java.io.tmpdir") + "/codegenerator-test";
        String basePackage = "com.codegenerator.test";
        boolean overwrite = true;

        // 2. 清空临时目录
        File tempDir = new File(outputPath);
        if (tempDir.exists()) {
            deleteDirectory(tempDir);
        }
        tempDir.mkdirs();

        try {
            // 3. 执行代码生成
            System.out.println("开始测试Controller模板生成...");
            codeGeneratorService.generateBackendCode(schemaName, tableName, outputPath, basePackage, overwrite);
            System.out.println("Controller代码生成完成。");

            // 4. 验证生成的Controller文件
            String className = "User";
            String controllerFilePath = outputPath + "/" + basePackage.replace('.', '/') + "/controller/" + className + "Controller.java";
            assertTrue(Files.exists(Paths.get(controllerFilePath)), "Controller文件应该被生成");

            // 5. 读取生成的Controller文件内容
            String controllerContent = new String(Files.readAllBytes(Paths.get(controllerFilePath)));
            System.out.println("生成的Controller文件内容长度: " + controllerContent.length() + " 字节");

            // 6. 验证修复的问题是否都已解决
            // 6.1 验证search参数处理是否正确实现
            assertTrue(controllerContent.contains("if (search != null && !search.isEmpty())"), 
                    "Controller应该包含search参数处理逻辑");
            assertTrue(controllerContent.contains("userRepository.findBySearchTerm"), 
                    "Controller应该使用正确的搜索方法");

            // 6.2 验证主键设置逻辑是否正确（动态设置，非硬编码）
            assertFalse(controllerContent.contains("set${PrimaryKeyField}"), 
                    "Controller不应该包含未解析的主键设置模板语法");
            assertTrue(controllerContent.contains("// 设置主键"), 
                    "Controller应该包含主键设置的注释");
            assertTrue(controllerContent.contains("Optional<" + className + "> existingEntity ="), 
                    "Controller应该包含检查实体是否存在的逻辑");

            // 6.3 验证Excel导出时的Cell变量命名是否正确
            assertTrue(controllerContent.contains("cell_"), 
                    "Controller的Excel导出方法中应该使用带索引的Cell变量名");
            assertFalse(controllerContent.contains("Cell cell ="), 
                    "Controller的Excel导出方法中不应该使用重复的Cell变量名");

            // 6.4 验证Excel导入时的类型处理逻辑是否完善
            assertTrue(controllerContent.contains("CellType.STRING"), 
                    "Controller的Excel导入方法应该处理STRING类型单元格");
            assertTrue(controllerContent.contains("CellType.NUMERIC"), 
                    "Controller的Excel导入方法应该处理NUMERIC类型单元格");
            assertTrue(controllerContent.contains("DateUtil.isCellDateFormatted"), 
                    "Controller的Excel导入方法应该处理日期类型单元格");
            assertTrue(controllerContent.contains("try {"), 
                    "Controller的Excel导入方法应该包含异常捕获逻辑");

            // 6.5 验证必要的import语句是否存在
            assertTrue(controllerContent.contains("import org.apache.poi.ss.usermodel.CellType;"), 
                    "Controller应该包含CellType的import语句");
            assertTrue(controllerContent.contains("import org.apache.poi.ss.usermodel.DateUtil;"), 
                    "Controller应该包含DateUtil的import语句");

            System.out.println("Controller模板测试通过！所有修复的问题都已正确实现。");

        } finally {
            // 清理临时文件（可选）
            // deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}