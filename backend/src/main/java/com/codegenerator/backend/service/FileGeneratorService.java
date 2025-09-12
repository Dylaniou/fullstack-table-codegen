package com.codegenerator.backend.service;

import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 文件生成服务类，负责文件的创建、写入和删除等操作
 */
@Service
public class FileGeneratorService {

    /**
     * 生成Java文件
     * @param data 模板数据
     * @param templateName 模板名称
     * @param outputPath 输出文件路径
     * @param templateService 模板服务
     * @throws IOException IO异常
     * @throws freemarker.template.TemplateException 模板异常
     */
    public void generateJavaFile(Map<String, Object> data, String templateName, String outputPath, 
                                TemplateRenderingService templateService) throws IOException, freemarker.template.TemplateException {
        // 创建目录（如果不存在）
        Path directoryPath = Paths.get(outputPath.substring(0, outputPath.lastIndexOf('/')));
        Files.createDirectories(directoryPath);

        // 写入文件，显式指定UTF-8编码以避免乱码问题
        try (Writer fileWriter = new OutputStreamWriter(
                new FileOutputStream(outputPath), StandardCharsets.UTF_8)) {
            templateService.renderTemplate(templateName, data, fileWriter);
        }
    }

    /**
     * 生成前端文件
     * @param data 模板数据
     * @param templateName 模板名称
     * @param outputPath 输出文件路径
     * @param templateService 模板服务
     * @throws IOException IO异常
     * @throws freemarker.template.TemplateException 模板异常
     */
    public void generateFrontendFile(Map<String, Object> data, String templateName, String outputPath, 
                                   TemplateRenderingService templateService) throws IOException, freemarker.template.TemplateException {
        // 创建目录（如果不存在）
        Path directoryPath = Paths.get(outputPath.substring(0, outputPath.lastIndexOf('/')));
        Files.createDirectories(directoryPath);

        // 写入文件，显式指定UTF-8编码以避免乱码问题
        try (Writer fileWriter = new OutputStreamWriter(
                new FileOutputStream(outputPath), StandardCharsets.UTF_8)) {
            templateService.renderTemplate(templateName, data, fileWriter);
        }
    }

    /**
     * 如果文件存在，则删除文件
     * @param filePath 文件路径
     */
    public void deleteFileIfExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("已删除文件：" + filePath);
            }
        } catch (IOException e) {
            System.err.println("删除文件失败：" + filePath + ", 错误：" + e.getMessage());
        }
    }

    /**
     * 删除多个文件
     * @param filePaths 文件路径列表
     */
    public void deleteFilesIfExists(List<String> filePaths) {
        for (String filePath : filePaths) {
            deleteFileIfExists(filePath);
        }
    }
}