package ${controllerPackage};

import ${modelPackage}.${className};
import ${repositoryPackage}.${className}Repository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

// 条件性添加日期时间相关的import语句
<#assign importsAdded = false />
<#if columns?exists && (columns?size > 0)>
<#list columns as column>
<#if (column.javaType == "Date" || column.javaType == "LocalDate" || column.javaType == "LocalDateTime") && !importsAdded>
<#assign importsAdded = true />
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
</#if>
</#list>
</#if>

@RestController
@RequestMapping("/api/${apiEndpoint}")
public class ${className}Controller {

    @Autowired
    private ${className}Repository ${repositoryName};

    // Get all entities with pagination
    @GetMapping
    public Page<${className}> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isEmpty()) {
            // 使用搜索方法查找匹配的数据
            return ${repositoryName}.findBySearchTerm(search, pageable);
        } else {
            return ${repositoryName}.findAll(pageable);
        }
    }

    // Get entity by ID
    @GetMapping("/{id}")
    public ResponseEntity<${className}> getById(@PathVariable ${idType} id) {
        return ${repositoryName}.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new entity
    @PostMapping
    public ${className} create(@RequestBody ${className} entity) {
        return ${repositoryName}.save(entity);
    }

    // Update an existing entity
    @PutMapping("/{id}")
    public ResponseEntity<${className}> update(@PathVariable ${idType} id, @RequestBody ${className} entity) {
        Optional<${className}> existingEntity = ${repositoryName}.findById(id);
        if (existingEntity.isPresent()) {
            // 设置主键值，避免被修改
            <#if columns?? && (columns?size != 0)>
                <#list columns as col>
                    <#if col.isPrimaryKey>
                        entity.set${col.field?cap_first}(id);
                    </#if>
                </#list>
            <#else>
                // 默认使用setId方法
                entity.setId(id);
            </#if>
            return ResponseEntity.ok(${repositoryName}.save(entity));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a entity
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ${idType} id) {
        return ${repositoryName}.findById(id)
                .map(entity -> {
                    ${repositoryName}.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Batch delete entities
    @DeleteMapping("/batch")
    public ResponseEntity<Void> batchDelete(@RequestBody List<${idType}> ids) {
        ${repositoryName}.deleteAllById(ids);
        return ResponseEntity.ok().build();
    }

    // Export data to Excel
    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response, @RequestParam(required = false) String search) throws IOException {
        // 根据search参数获取数据
        List<${className}> entities;
        if (search != null && !search.isEmpty()) {
            entities = ${repositoryName}.findBySearchTerm(search);
        } else {
            entities = ${repositoryName}.findAll();
        }
        
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("${className}");
        
        // 创建表头行
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // 添加表头（这里应该根据实际模型字段调整）
        int headerCellNum = 0;
        <#list columns as column>
        // 使用唯一的变量名避免重复定义
        Cell headerCell_${column_index} = headerRow.createCell(headerCellNum++);
        headerCell_${column_index}.setCellValue("${column.fieldLabel}");
        headerCell_${column_index}.setCellStyle(headerStyle);
        </#list>
        
        // 添加数据行
        int rowNum = 1;
        for (${className} entity : entities) {
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            <#list columns as column>
            // 使用唯一的变量名避免重复定义
            Cell rowCell_${column_index} = row.createCell(cellNum++);
            rowCell_${column_index}.setCellValue(entity.get${column.field?cap_first}() != null ? entity.get${column.field?cap_first}().toString() : "");
            </#list>
        }
        
        // 调整列宽
        for (int i = 0; i < headerCellNum; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${kebabCaseName}-export.xlsx");
        
        // 写入响应
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    // Import data from Excel
    @PostMapping("/import")
    public ResponseEntity<String> importFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        InputStream inputStream = null;
        Workbook workbook = null;
        try {
            inputStream = file.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // 跳过表头行
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            List<${className}> entities = new ArrayList<>();
            
            // 读取数据行
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                ${className} entity = new ${className}();
                
                // 根据实际模型字段设置值
                int cellNum = 0;
                <#list columns as column>
                <#-- 这是FreeMarker的条件判断，决定是否生成以下Java代码 -->
                <#if !column.isPrimaryKey || !column.autoIncrement>
                // 使用唯一的变量名避免重复定义
                Cell cell_${column_index} = row.getCell(cellNum++);
                if (cell_${column_index} != null) {
                    try {
                        // 根据字段类型进行适当的转换
                        <#if column.javaType == "Integer">
                            // Integer类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.NUMERIC) {
                                entity.set${column.field?cap_first}((int)cell_${column_index}.getNumericCellValue());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        entity.set${column.field?cap_first}(Integer.parseInt(stringValue.trim()));
                                    } catch (NumberFormatException e) {
                                        // 忽略格式错误的数值
                                    }
                                }
                            }
                        <#elseif column.javaType == "Long">
                            // Long类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.NUMERIC) {
                                entity.set${column.field?cap_first}((long)cell_${column_index}.getNumericCellValue());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        entity.set${column.field?cap_first}(Long.parseLong(stringValue.trim()));
                                    } catch (NumberFormatException e) {
                                        // 忽略格式错误的数值
                                    }
                                }
                            }
                        <#elseif column.javaType == "Double">
                            // Double类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.NUMERIC) {
                                entity.set${column.field?cap_first}(cell_${column_index}.getNumericCellValue());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        entity.set${column.field?cap_first}(Double.parseDouble(stringValue.trim()));
                                    } catch (NumberFormatException e) {
                                        // 忽略格式错误的数值
                                    }
                                }
                            }
                        <#elseif column.javaType == "Float">
                            // Float类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.NUMERIC) {
                                entity.set${column.field?cap_first}((float)cell_${column_index}.getNumericCellValue());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        entity.set${column.field?cap_first}(Float.parseFloat(stringValue.trim()));
                                    } catch (NumberFormatException e) {
                                        // 忽略格式错误的数值
                                    }
                                }
                            }
                        <#elseif column.javaType == "Date">
                            // Date类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell_${column_index})) {
                                entity.set${column.field?cap_first}(cell_${column_index}.getDateCellValue());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        entity.set${column.field?cap_first}(dateFormat.parse(stringValue.trim()));
                                    } catch (ParseException e) {
                                        // 忽略格式错误的日期
                                    }
                                }
                            }
                        <#elseif column.javaType == "LocalDate">
                            // LocalDate类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell_${column_index})) {
                                Date dateValue = cell_${column_index}.getDateCellValue();
                                entity.set${column.field?cap_first}(dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                        entity.set${column.field?cap_first}(LocalDate.parse(stringValue.trim(), formatter));
                                    } catch (Exception e) {
                                        // 忽略格式错误的日期
                                    }
                                }
                            }
                        <#elseif column.javaType == "LocalDateTime">
                            // LocalDateTime类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell_${column_index})) {
                                Date dateValue = cell_${column_index}.getDateCellValue();
                                entity.set${column.field?cap_first}(dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        entity.set${column.field?cap_first}(LocalDateTime.parse(stringValue.trim(), formatter));
                                    } catch (Exception e) {
                                        // 忽略格式错误的日期时间
                                    }
                                }
                            }
                        <#elseif column.javaType == "Boolean">
                            // Boolean类型字段的处理
                            if (cell_${column_index}.getCellType() == CellType.BOOLEAN) {
                                entity.set${column.field?cap_first}(cell_${column_index}.getBooleanCellValue());
                            } else if (cell_${column_index}.getCellType() == CellType.STRING) {
                                String stringValue = cell_${column_index}.getStringCellValue();
                                if ("true".equalsIgnoreCase(stringValue.trim()) || "1".equals(stringValue.trim())) {
                                    entity.set${column.field?cap_first}(Boolean.TRUE);
                                } else if ("false".equalsIgnoreCase(stringValue.trim()) || "0".equals(stringValue.trim())) {
                                    entity.set${column.field?cap_first}(Boolean.FALSE);
                                }
                            } else if (cell_${column_index}.getCellType() == CellType.NUMERIC) {
                                double numericValue = cell_${column_index}.getNumericCellValue();
                                entity.set${column.field?cap_first}(numericValue != 0);
                            }
                        <#else>
                            // 默认为字符串类型，直接使用字符串值
                            if (cell_${column_index}.getCellType() == CellType.STRING) {
                                entity.set${column.field?cap_first}(cell_${column_index}.getStringCellValue());
                            } else if (cell_${column_index}.getCellType() == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell_${column_index})) {
                                    entity.set${column.field?cap_first}(cell_${column_index}.getDateCellValue().toString());
                                } else {
                                    entity.set${column.field?cap_first}(String.valueOf(cell_${column_index}.getNumericCellValue()));
                                }
                            } else if (cell_${column_index}.getCellType() == CellType.BOOLEAN) {
                                entity.set${column.field?cap_first}(String.valueOf(cell_${column_index}.getBooleanCellValue()));
                            }
                        </#if>
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field ${column.field}: " + e.getMessage());
                    }
                }
                </#if>
                </#list>
                
                entities.add(entity);
            }
            
            // 保存数据
            ${repositoryName}.saveAll(entities);
            
            return ResponseEntity.ok().body("Import successful: " + entities.size() + " records imported");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to import data: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}