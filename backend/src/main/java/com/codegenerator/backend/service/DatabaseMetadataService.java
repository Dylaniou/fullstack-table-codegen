package com.codegenerator.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 数据库元数据服务类，负责获取数据库表和列的元数据信息
 */
@Service
public class DatabaseMetadataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取指定schema中的所有表名
     * @param schemaName 数据库schema名称
     * @return 表名列表
     * @throws SQLException SQL异常
     */
    public List<String> getAllTables(String schemaName) throws SQLException {
        List<String> tables = new ArrayList<>();
        
        // 使用try-with-resources自动关闭连接
        try (Connection connection = jdbcTemplate.getDataSource().getConnection();
             ResultSet resultSet = connection.getMetaData().getTables(null, schemaName, "%", new String[] {"TABLE"})) {
            
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));
            }
        }
        
        return tables;
    }

    /**
     * 获取指定schema中表的元数据
     * @param schemaName 数据库schema名称
     * @param tableName 表名
     * @return 表元数据
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getTableMetadata(String schemaName, String tableName) throws SQLException {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> columns = new ArrayList<>();
        String primaryKey = null;

        // 使用try-with-resources自动关闭连接和结果集
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            // 对于MySQL，第一个参数是catalog（即数据库名），第二个参数是schema
            String catalog = connection.getCatalog();
            
            try (ResultSet columnsResultSet = connection.getMetaData().getColumns(catalog, null, tableName, null);
                 ResultSet pkResultSet = connection.getMetaData().getPrimaryKeys(catalog, null, tableName)) {
                
                // 获取列信息
                while (columnsResultSet.next()) {
                    Map<String, Object> column = new HashMap<>();
                    String columnName = columnsResultSet.getString("COLUMN_NAME");
                    String typeName = columnsResultSet.getString("TYPE_NAME");
                    int nullable = columnsResultSet.getInt("NULLABLE");
                    boolean isAutoIncrement = false;
                    try {
                        isAutoIncrement = columnsResultSet.getBoolean("IS_AUTOINCREMENT");
                    } catch (SQLException e) {
                        // 某些数据库可能不支持IS_AUTOINCREMENT列
                    }

                    column.put("columnName", columnName);
                    column.put("fieldName", camelCase(columnName));
                    column.put("field", camelCase(columnName)); // 添加field属性，与fieldName保持一致
                    column.put("displayName", displayName(columnName));
                    column.put("fieldLabel", displayName(columnName)); // 为Vue模板添加fieldLabel
                    column.put("javaType", getJavaType(typeName));
                    column.put("jdbcType", typeName); // 添加jdbcType字段，用于Excel导入功能
                    column.put("htmlInputType", getHtmlInputType(typeName));
                    column.put("isRequired", nullable == 0);
                    column.put("nullable", nullable != 0); // 为Vue模板添加nullable字段
                    column.put("defaultValue", getDefaultValue(getJavaType(typeName)));
                    column.put("annotation", getJpaAnnotation(typeName));
                    column.put("isPrimaryKey", false); // 默认所有列不是主键
                    column.put("autoIncrement", isAutoIncrement);

                    columns.add(column);
                }

                // 获取主键
                if (pkResultSet.next()) {
                    primaryKey = pkResultSet.getString("COLUMN_NAME");
                    for (Map<String, Object> column : columns) {
                        if (column.get("columnName").equals(primaryKey)) {
                            column.put("isPrimaryKey", true);
                            break;
                        }
                    }
                }
            }
        }

        data.put("tableName", tableName);
        data.put("className", pascalCase(tableName));
        data.put("columns", columns);
        data.put("primaryKeyField", primaryKey != null ? camelCase(primaryKey) : null);
        data.put("primaryKey", primaryKey); // 添加primaryKey变量到数据模型中
        data.put("apiEndpoint", tableName.toLowerCase());
        data.put("componentName", pascalCase(tableName));
        
        // 设置主键类型用于Repository
        if (primaryKey != null) {
            for (Map<String, Object> column : columns) {
                if (column.get("columnName").equals(primaryKey)) {
                    data.put("idType", column.get("javaType"));
                    break;
                }
            }
        } else {
            data.put("idType", "Long"); // 默认主键类型
        }
        
        // 设置repositoryName变量用于Controller
        data.put("repositoryName", camelCase(tableName) + "Repository");
        // 设置kebabCaseName变量用于Vue组件
        data.put("kebabCaseName", kebabCase(tableName));
        
        // 添加首字母大写的PrimaryKeyField变量，与controller.ftl模板中的使用保持一致
        data.put("PrimaryKeyField", data.get("primaryKeyField"));

        return data;
    }

    /**
     * 驼峰命名转换
     */
    private String camelCase(String input) {
        String[] parts = input.toLowerCase().split("_+");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(Character.toUpperCase(parts[i].charAt(0)))
                  .append(parts[i].substring(1));
        }
        return result.toString();
    }

    /**
     * 首字母大写命名转换
     */
    private String pascalCase(String input) {
        String camelCase = camelCase(input);
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }

    /**
     * 显示名称转换
     */
    private String displayName(String columnName) {
        String[] parts = columnName.toLowerCase().split("_+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
        }
        return result.toString().trim();
    }
    
    /**
     * Kebab Case命名转换（短横线分隔的小写形式）
     */
    private String kebabCase(String input) {
        return input.toLowerCase().replaceAll("_+", "-");
    }

    /**
     * 将SQL类型转换为Java类型
     */
    private String getJavaType(String sqlType) {
        switch (sqlType.toUpperCase()) {
            case "INT":
            case "INTEGER":
                return "Integer";
            case "BIGINT":
                return "Long";
            case "VARCHAR":
            case "TEXT":
            case "CHAR":
                return "String";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
                return "LocalDateTime";
            case "BOOLEAN":
            case "BIT":
                return "Boolean";
            case "DOUBLE":
            case "FLOAT":
            case "DECIMAL":
                return "Double";
            default:
                return "String";
        }
    }

    /**
     * 获取HTML输入类型
     */
    private String getHtmlInputType(String sqlType) {
        switch (sqlType.toUpperCase()) {
            case "INT":
            case "INTEGER":
            case "BIGINT":
            case "DOUBLE":
            case "FLOAT":
            case "DECIMAL":
                return "number";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
                return "datetime-local";
            case "BOOLEAN":
            case "BIT":
                return "checkbox";
            default:
                return "text";
        }
    }

    /**
     * 获取默认值
     */
    private String getDefaultValue(String javaType) {
        switch (javaType) {
            case "Integer":
            case "Long":
            case "Double":
                return "0";
            case "Boolean":
                return "false";
            case "LocalDateTime":
                return "null";
            default:
                return "''";
        }
    }

    /**
     * 获取JPA注解
     */
    private String getJpaAnnotation(String sqlType) {
        switch (sqlType.toUpperCase()) {
            case "VARCHAR":
            case "TEXT":
                return "Column(length = 255)";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
                return "Column(columnDefinition = \"DATETIME\")";
            default:
                return "Column";
        }
    }
}