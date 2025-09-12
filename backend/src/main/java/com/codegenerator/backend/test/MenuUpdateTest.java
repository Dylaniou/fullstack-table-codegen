package com.codegenerator.backend.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MenuUpdateTest {

    public static void main(String[] args) {
        System.out.println("===== 开始测试菜单更新功能 =====");
        
        try {
            // 要添加的新菜单项
            String componentName = "NewModule";
            String layoutFilePath = "e:/001home/hjj/004project/0823test/0906/frontend/src/layouts/MainLayout.vue";
            
            // 读取当前布局文件内容进行预览
            String layoutContent = new String(Files.readAllBytes(Paths.get(layoutFilePath)));
            int menuEndIndex = layoutContent.lastIndexOf("</el-menu>");
            if (menuEndIndex > 0) {
                // 打印el-menu结束标签附近的内容
                int startPreview = Math.max(0, menuEndIndex - 100);
                int endPreview = Math.min(layoutContent.length(), menuEndIndex + 50);
                System.out.println("当前el-menu结束标签附近内容：");
                System.out.println(layoutContent.substring(startPreview, endPreview));
            }
            
            // 模拟updateLayoutNavigation方法的最新逻辑
            updateLayoutNavigation(componentName, layoutFilePath);
            
            // 读取更新后的布局文件内容进行验证
            String updatedContent = new String(Files.readAllBytes(Paths.get(layoutFilePath)));
            String routePath = "/" + componentName.toLowerCase();
            if (updatedContent.contains("index=\"" + routePath + "\"")) {
                System.out.println("测试成功：菜单项已成功添加到布局文件中");
                
                // 显示更新后的el-menu结束标签附近内容，验证缩进格式
                int updatedMenuEndIndex = updatedContent.lastIndexOf("</el-menu>");
                if (updatedMenuEndIndex > 0) {
                    int startPreview = Math.max(0, updatedMenuEndIndex - 150);
                    int endPreview = Math.min(updatedContent.length(), updatedMenuEndIndex + 50);
                    System.out.println("更新后的el-menu结束标签附近内容：");
                    System.out.println(updatedContent.substring(startPreview, endPreview));
                }
            } else {
                System.out.println("测试失败：菜单项未添加到布局文件中");
            }
            
        } catch (Exception e) {
            System.err.println("测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===== 测试菜单更新功能结束 =====");
    }
    
    private static void updateLayoutNavigation(String componentName, String layoutFilePath) throws IOException {
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
    }
}