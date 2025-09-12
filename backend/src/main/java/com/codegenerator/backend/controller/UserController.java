package com.codegenerator.backend.controller;

import com.codegenerator.backend.model.User;
import com.codegenerator.backend.repository.UserRepository;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Get all entities with pagination
    @GetMapping
    public Page<User> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isEmpty()) {
            // 使用搜索方法查找匹配的数据
            return userRepository.findBySearchTerm(search, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    // Get entity by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new entity
    @PostMapping
    public User create(@RequestBody User entity) {
        return userRepository.save(entity);
    }

    // Update an existing entity
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Integer id, @RequestBody User entity) {
        Optional<User> existingEntity = userRepository.findById(id);
        if (existingEntity.isPresent()) {
            // 设置主键值，避免被修改
                        entity.setId(id);
            return ResponseEntity.ok(userRepository.save(entity));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a entity
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(entity -> {
                    userRepository.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Batch delete entities
    @DeleteMapping("/batch")
    public ResponseEntity<Void> batchDelete(@RequestBody List<Integer> ids) {
        userRepository.deleteAllById(ids);
        return ResponseEntity.ok().build();
    }

    // Export data to Excel
    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response, @RequestParam(required = false) String search) throws IOException {
        // 根据search参数获取数据
        List<User> entities;
        if (search != null && !search.isEmpty()) {
            entities = userRepository.findBySearchTerm(search);
        } else {
            entities = userRepository.findAll();
        }
        
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("User");
        
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
        // 使用唯一的变量名避免重复定义
        Cell headerCell_0 = headerRow.createCell(headerCellNum++);
        headerCell_0.setCellValue("Id");
        headerCell_0.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_1 = headerRow.createCell(headerCellNum++);
        headerCell_1.setCellValue("Mobile");
        headerCell_1.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_2 = headerRow.createCell(headerCellNum++);
        headerCell_2.setCellValue("Passwd");
        headerCell_2.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_3 = headerRow.createCell(headerCellNum++);
        headerCell_3.setCellValue("Name");
        headerCell_3.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_4 = headerRow.createCell(headerCellNum++);
        headerCell_4.setCellValue("Sex");
        headerCell_4.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_5 = headerRow.createCell(headerCellNum++);
        headerCell_5.setCellValue("Age");
        headerCell_5.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_6 = headerRow.createCell(headerCellNum++);
        headerCell_6.setCellValue("Birthday");
        headerCell_6.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_7 = headerRow.createCell(headerCellNum++);
        headerCell_7.setCellValue("Area");
        headerCell_7.setCellStyle(headerStyle);
        // 使用唯一的变量名避免重复定义
        Cell headerCell_8 = headerRow.createCell(headerCellNum++);
        headerCell_8.setCellValue("Score");
        headerCell_8.setCellStyle(headerStyle);
        
        // 添加数据行
        int rowNum = 1;
        for (User entity : entities) {
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            // 使用唯一的变量名避免重复定义
            Cell rowCell_0 = row.createCell(cellNum++);
            rowCell_0.setCellValue(entity.getId() != null ? entity.getId().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_1 = row.createCell(cellNum++);
            rowCell_1.setCellValue(entity.getMobile() != null ? entity.getMobile().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_2 = row.createCell(cellNum++);
            rowCell_2.setCellValue(entity.getPasswd() != null ? entity.getPasswd().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_3 = row.createCell(cellNum++);
            rowCell_3.setCellValue(entity.getName() != null ? entity.getName().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_4 = row.createCell(cellNum++);
            rowCell_4.setCellValue(entity.getSex() != null ? entity.getSex().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_5 = row.createCell(cellNum++);
            rowCell_5.setCellValue(entity.getAge() != null ? entity.getAge().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_6 = row.createCell(cellNum++);
            rowCell_6.setCellValue(entity.getBirthday() != null ? entity.getBirthday().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_7 = row.createCell(cellNum++);
            rowCell_7.setCellValue(entity.getArea() != null ? entity.getArea().toString() : "");
            // 使用唯一的变量名避免重复定义
            Cell rowCell_8 = row.createCell(cellNum++);
            rowCell_8.setCellValue(entity.getScore() != null ? entity.getScore().toString() : "");
        }
        
        // 调整列宽
        for (int i = 0; i < headerCellNum; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-export.xlsx");
        
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
            
            List<User> entities = new ArrayList<>();
            
            // 读取数据行
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                User entity = new User();
                
                // 根据实际模型字段设置值
                int cellNum = 0;
                // 使用唯一的变量名避免重复定义
                Cell cell_1 = row.getCell(cellNum++);
                if (cell_1 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // 默认为字符串类型，直接使用字符串值
                            if (cell_1.getCellType() == CellType.STRING) {
                                entity.setMobile(cell_1.getStringCellValue());
                            } else if (cell_1.getCellType() == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell_1)) {
                                    entity.setMobile(cell_1.getDateCellValue().toString());
                                } else {
                                    entity.setMobile(String.valueOf(cell_1.getNumericCellValue()));
                                }
                            } else if (cell_1.getCellType() == CellType.BOOLEAN) {
                                entity.setMobile(String.valueOf(cell_1.getBooleanCellValue()));
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field mobile: " + e.getMessage());
                    }
                }
                // 使用唯一的变量名避免重复定义
                Cell cell_2 = row.getCell(cellNum++);
                if (cell_2 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // 默认为字符串类型，直接使用字符串值
                            if (cell_2.getCellType() == CellType.STRING) {
                                entity.setPasswd(cell_2.getStringCellValue());
                            } else if (cell_2.getCellType() == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell_2)) {
                                    entity.setPasswd(cell_2.getDateCellValue().toString());
                                } else {
                                    entity.setPasswd(String.valueOf(cell_2.getNumericCellValue()));
                                }
                            } else if (cell_2.getCellType() == CellType.BOOLEAN) {
                                entity.setPasswd(String.valueOf(cell_2.getBooleanCellValue()));
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field passwd: " + e.getMessage());
                    }
                }
                // 使用唯一的变量名避免重复定义
                Cell cell_3 = row.getCell(cellNum++);
                if (cell_3 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // 默认为字符串类型，直接使用字符串值
                            if (cell_3.getCellType() == CellType.STRING) {
                                entity.setName(cell_3.getStringCellValue());
                            } else if (cell_3.getCellType() == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell_3)) {
                                    entity.setName(cell_3.getDateCellValue().toString());
                                } else {
                                    entity.setName(String.valueOf(cell_3.getNumericCellValue()));
                                }
                            } else if (cell_3.getCellType() == CellType.BOOLEAN) {
                                entity.setName(String.valueOf(cell_3.getBooleanCellValue()));
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field name: " + e.getMessage());
                    }
                }
                // 使用唯一的变量名避免重复定义
                Cell cell_4 = row.getCell(cellNum++);
                if (cell_4 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // 默认为字符串类型，直接使用字符串值
                            if (cell_4.getCellType() == CellType.STRING) {
                                entity.setSex(cell_4.getStringCellValue());
                            } else if (cell_4.getCellType() == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell_4)) {
                                    entity.setSex(cell_4.getDateCellValue().toString());
                                } else {
                                    entity.setSex(String.valueOf(cell_4.getNumericCellValue()));
                                }
                            } else if (cell_4.getCellType() == CellType.BOOLEAN) {
                                entity.setSex(String.valueOf(cell_4.getBooleanCellValue()));
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field sex: " + e.getMessage());
                    }
                }
                // 使用唯一的变量名避免重复定义
                Cell cell_5 = row.getCell(cellNum++);
                if (cell_5 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // 默认为字符串类型，直接使用字符串值
                            if (cell_5.getCellType() == CellType.STRING) {
                                entity.setAge(cell_5.getStringCellValue());
                            } else if (cell_5.getCellType() == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell_5)) {
                                    entity.setAge(cell_5.getDateCellValue().toString());
                                } else {
                                    entity.setAge(String.valueOf(cell_5.getNumericCellValue()));
                                }
                            } else if (cell_5.getCellType() == CellType.BOOLEAN) {
                                entity.setAge(String.valueOf(cell_5.getBooleanCellValue()));
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field age: " + e.getMessage());
                    }
                }
                // 使用唯一的变量名避免重复定义
                Cell cell_6 = row.getCell(cellNum++);
                if (cell_6 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // LocalDateTime类型字段的处理
                            if (cell_6.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell_6)) {
                                Date dateValue = cell_6.getDateCellValue();
                                entity.setBirthday(dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                            } else if (cell_6.getCellType() == CellType.STRING) {
                                String stringValue = cell_6.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        entity.setBirthday(LocalDateTime.parse(stringValue.trim(), formatter));
                                    } catch (Exception e) {
                                        // 忽略格式错误的日期时间
                                    }
                                }
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field birthday: " + e.getMessage());
                    }
                }
                // 使用唯一的变量名避免重复定义
                Cell cell_7 = row.getCell(cellNum++);
                if (cell_7 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // 默认为字符串类型，直接使用字符串值
                            if (cell_7.getCellType() == CellType.STRING) {
                                entity.setArea(cell_7.getStringCellValue());
                            } else if (cell_7.getCellType() == CellType.NUMERIC) {
                                if (DateUtil.isCellDateFormatted(cell_7)) {
                                    entity.setArea(cell_7.getDateCellValue().toString());
                                } else {
                                    entity.setArea(String.valueOf(cell_7.getNumericCellValue()));
                                }
                            } else if (cell_7.getCellType() == CellType.BOOLEAN) {
                                entity.setArea(String.valueOf(cell_7.getBooleanCellValue()));
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field area: " + e.getMessage());
                    }
                }
                // 使用唯一的变量名避免重复定义
                Cell cell_8 = row.getCell(cellNum++);
                if (cell_8 != null) {
                    try {
                        // 根据字段类型进行适当的转换
                            // Double类型字段的处理
                            if (cell_8.getCellType() == CellType.NUMERIC) {
                                entity.setScore(cell_8.getNumericCellValue());
                            } else if (cell_8.getCellType() == CellType.STRING) {
                                String stringValue = cell_8.getStringCellValue();
                                if (!stringValue.isEmpty()) {
                                    try {
                                        entity.setScore(Double.parseDouble(stringValue.trim()));
                                    } catch (NumberFormatException e) {
                                        // 忽略格式错误的数值
                                    }
                                }
                            }
                    } catch (Exception e) {
                        // 类型转换失败时记录错误但继续处理
                        System.err.println("Failed to parse cell value for field score: " + e.getMessage());
                    }
                }
                
                entities.add(entity);
            }
            
            // 保存数据
            userRepository.saveAll(entities);
            
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