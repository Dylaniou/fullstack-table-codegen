package com.codegenerator.backend.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 独立的布局文件更新测试类
 * 可以直接运行来测试updateLayoutNavigation方法的逻辑
 */
public class LayoutUpdateTest {

    public static void main(String[] args) {
        System.out.println("===== 开始测试布局文件更新功能 =====");
        
        try {
            // 调用模拟的updateLayoutNavigation方法
            String componentName = "User";
            String layoutFilePath = "e:/001home/hjj/004project/0823test/0906/frontend/src/layouts/MainLayout.vue";
            
            // 执行更新操作
            simulateUpdateLayoutNavigation(componentName, layoutFilePath);
            
            System.out.println("测试完成：已尝试添加User Management菜单项到布局文件");
            System.out.println("请检查MainLayout.vue文件中是否已添加了新菜单项");
            
        } catch (Exception e) {
            System.err.println("测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===== 测试布局文件更新功能结束 =====");
    }
    
    /**
     * 模拟updateLayoutNavigation方法的功能
     */
    private static void simulateUpdateLayoutNavigation(String componentName, String layoutFilePath) throws IOException {
        if (!Files.exists(Paths.get(layoutFilePath))) {
            System.out.println("布局文件不存在: " + layoutFilePath);
            return;
        }
        
        // 读取现有的布局文件内容
        List<String> lines = Files.readAllLines(Paths.get(layoutFilePath));
        System.out.println("已读取布局文件，共" + lines.size() + "行");
        
        // 打印部分内容用于调试
        System.out.println("布局文件部分内容预览：");
        for (int i = 20; i < Math.min(40, lines.size()); i++) {
            System.out.println("第" + (i + 1) + "行: " + lines.get(i));
        }
        
        // 检查是否已存在该组件的导航链接
        String routePath = "/" + componentName.toLowerCase();
        String navText = componentName + " Management";
        boolean navLinkExists = false;
        
        for (String line : lines) {
            if (line.contains("el-menu-item") && line.contains("index=\"" + routePath + "\"")) {
                navLinkExists = true;
                break;
            }
        }
        
        if (navLinkExists) {
            System.out.println("菜单项已存在，无需添加: " + navText);
            return;
        }
        
        // 查找el-menu标签的位置和内部的el-menu-item列表
        int menuStartIndex = -1;
        int menuEndIndex = -1;
        int lastMenuItemIndex = -1;
        
        // 先找到el-menu开始和结束标签
        // 使用更宽松的匹配方式，只需要包含<el-menu，不强制要求同时包含router
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("<el-menu")) {
                menuStartIndex = i;
                System.out.println("找到el-menu开始标签，行号: " + (i + 1));
                System.out.println("标签内容: " + lines.get(i));
            }
            if (lines.get(i).contains("</el-menu>")) {
                menuEndIndex = i;
                System.out.println("找到el-menu结束标签，行号: " + (i + 1));
                break;
            }
        }
        
        // 在el-menu内部找到最后一个el-menu-item的位置
        if (menuStartIndex >= 0 && menuEndIndex >= 0) {
            for (int i = menuStartIndex; i < menuEndIndex; i++) {
                if (lines.get(i).contains("<el-menu-item")) {
                    lastMenuItemIndex = i;
                }
            }
            System.out.println("找到最后一个el-menu-item，行号: " + (lastMenuItemIndex + 1));
            
            // 构建新的导航链接（Element Plus风格）
            String newNavLink = "          <el-menu-item index=\"" + routePath + "\">\n" +
                               "            <el-icon><Code /></el-icon>\n" +
                               "            <span>" + navText + "</span>\n" +
                               "          </el-menu-item>";
            
            // 如果找到了最后一个菜单项，就在它后面添加；否则就添加在el-menu开始标签后
            int insertIndex = (lastMenuItemIndex >= 0) ? lastMenuItemIndex + 1 : menuStartIndex + 1;
            System.out.println("准备在第" + (insertIndex + 1) + "行插入新菜单项");
            
            // 将新行拆分成单独的行
            List<String> newLines = new ArrayList<>();
            for (String line : lines) {
                newLines.add(line);
            }
            
            // 插入新的菜单项（保持正确的缩进和格式）
            String[] navLines = newNavLink.split("\\n");
            for (int i = navLines.length - 1; i >= 0; i--) {
                newLines.add(insertIndex, navLines[i]);
            }
            
            // 写回文件
            Files.write(Paths.get(layoutFilePath), newLines);
            System.out.println("已添加新菜单项: " + navText + "，路径: " + routePath);
        } else {
            System.out.println("未找到el-menu标签，无法添加菜单项");
        }
    }
}