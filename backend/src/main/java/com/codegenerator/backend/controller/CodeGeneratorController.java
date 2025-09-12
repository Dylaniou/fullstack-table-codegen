package com.codegenerator.backend.controller;

import com.codegenerator.backend.exception.CodeGenerationException;
import com.codegenerator.backend.exception.DatabaseAccessException;
import com.codegenerator.backend.service.CodeGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器，提供代码生成相关的API接口
 */
@RestController
@RequestMapping("/api/codegen")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CodeGeneratorController {

    @Autowired
    private CodeGeneratorService codeGeneratorService;

    /**
     * 获取指定schema中的所有表名
     */
    @GetMapping("/tables/{schemaName}")
    public ResponseEntity<List<String>> getAllTables(@PathVariable String schemaName) {
        try {
            List<String> tables = codeGeneratorService.getAllTables(schemaName);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            throw new DatabaseAccessException("获取表列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定表的元数据
     */
    @GetMapping("/metadata/{schemaName}/{tableName}")
    public ResponseEntity<Map<String, Object>> getTableMetadata(@PathVariable String schemaName, @PathVariable String tableName) {
        try {
            Map<String, Object> metadata = codeGeneratorService.getTableMetadata(schemaName, tableName);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            throw new DatabaseAccessException("获取表元数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成指定表的后端代码
     */
    @PostMapping("/generate/backend/{schemaName}/{tableName}")
    public ResponseEntity<String> generateBackendCode(@PathVariable String schemaName, @PathVariable String tableName, @RequestBody Map<String, Object> requestBody) {
        try {
            String outputPath = requestBody.getOrDefault("outputPath", "").toString();
            String basePackage = requestBody.getOrDefault("basePackage", "").toString();
            Boolean overwrite = Boolean.valueOf(requestBody.getOrDefault("overwrite", false).toString());
            
            codeGeneratorService.generateBackendCode(schemaName, tableName, outputPath, basePackage, overwrite);
            return ResponseEntity.ok("后端代码生成成功");
        } catch (Exception e) {
            throw new CodeGenerationException("生成后端代码失败: " + e.getMessage(), e);
        }
    }

    

    
    
    /**
     * 生成指定表的前端代码
     */
    @PostMapping("/generate/frontend/{schemaName}/{tableName}")
    public ResponseEntity<String> generateFrontendCode(@PathVariable String schemaName, @PathVariable String tableName, @RequestBody Map<String, Object> requestBody) {
        try {
            String outputPath = requestBody.getOrDefault("outputPath", "").toString();
            Boolean overwrite = Boolean.valueOf(requestBody.getOrDefault("overwrite", false).toString());
            
            codeGeneratorService.generateFrontendCode(schemaName, tableName, outputPath, overwrite);
            return ResponseEntity.ok("前端代码生成成功");
        } catch (Exception e) {
            throw new CodeGenerationException("生成前端代码失败: " + e.getMessage(), e);
        }
    }

    

    
    
    /**
     * 生成指定表的全栈代码
     */
    @PostMapping("/generate/fullstack/{schemaName}/{tableName}")
    public ResponseEntity<String> generateFullstackCode(@PathVariable String schemaName, @PathVariable String tableName, @RequestBody Map<String, Object> requestBody) {
        try {
            String outputPath = requestBody.getOrDefault("outputPath", "").toString();
            String basePackage = requestBody.getOrDefault("basePackage", "").toString();
            Boolean overwrite = Boolean.valueOf(requestBody.getOrDefault("overwrite", false).toString());
            
            // 直接调用Service层的全栈代码生成方法，该方法具有完整的回滚机制
            codeGeneratorService.generateFullstackCode(schemaName, tableName, outputPath, basePackage, overwrite);
            return ResponseEntity.ok("全栈代码生成成功");
        } catch (Exception e) {
            throw new CodeGenerationException("生成全栈代码失败: " + e.getMessage(), e);
        }
    }

    

    
    
    /**
     * 批量生成代码
     */
    @PostMapping("/generate/batch/{schemaName}")
    public ResponseEntity<String> batchGenerateCode(@PathVariable String schemaName, @RequestBody Map<String, Object> requestBody) {
        try {
            List<String> tableNames = (List<String>) requestBody.get("tableNames");
            String outputPath = requestBody.getOrDefault("outputPath", "").toString();
            String basePackage = requestBody.getOrDefault("basePackage", "").toString();
            Boolean overwrite = Boolean.valueOf(requestBody.getOrDefault("overwrite", false).toString());
            
            codeGeneratorService.batchGenerateCode(schemaName, tableNames, outputPath, basePackage, overwrite);
            return ResponseEntity.ok("批量代码生成成功");
        } catch (Exception e) {
            throw new CodeGenerationException("批量生成代码失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除生成的代码
     */
    @DeleteMapping("/delete/{schemaName}/{tableName}")
    public ResponseEntity<String> deleteGeneratedCode(@PathVariable String schemaName, @PathVariable String tableName, @RequestBody Map<String, Object> requestBody) {
        try {
            String deleteType = requestBody.getOrDefault("deleteType", "fullstack").toString();
            String outputPath = requestBody.getOrDefault("outputPath", "").toString();
            
            codeGeneratorService.deleteGeneratedCode(schemaName, tableName, deleteType, outputPath);
            return ResponseEntity.ok("代码删除成功");
        } catch (Exception e) {
            throw new CodeGenerationException("删除代码失败: " + e.getMessage(), e);
        }
    }
}