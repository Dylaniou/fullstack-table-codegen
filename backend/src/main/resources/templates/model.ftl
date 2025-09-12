package ${modelPackage};

import javax.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
<#-- 根据字段类型动态添加必要的import语句 -->
<#if (idType == 'LocalDateTime') || ((columns?filter(c -> c.javaType == 'LocalDateTime')?size) > 0)>
import java.time.LocalDateTime;
</#if>

@Entity
@Table(name = "${tableName}")
@Schema(name = "${className}", description = "${className} entity representing ${tableName} data")
public class ${className} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "${primaryKey}")
    @Schema(description = "Unique identifier", example = "1", readOnly = true)
    private ${idType} ${primaryKeyField};

<#list columns as column>
    <#if !column.isPrimaryKey>
    @Column(name = "${column.columnName}")
    @Schema(description = "${column.displayName}")
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
    // Getters and Setters
    public ${idType} get${primaryKeyField?cap_first}() {
        return ${primaryKeyField};
    }

    public void set${primaryKeyField?cap_first}(${idType} ${primaryKeyField}) {
        this.${primaryKeyField} = ${primaryKeyField};
    }

<#list columns as column>
    <#if !column.isPrimaryKey>
    public ${column.javaType} get${column.fieldName?cap_first}() {
        return ${column.fieldName};
    }

    public void set${column.fieldName?cap_first}(${column.javaType} ${column.fieldName}) {
        this.${column.fieldName} = ${column.fieldName};
    }

    </#if>
</#list>
    // toString method
    @Override
    public String toString() {
        return "${className}[${primaryKeyField}=" + ${primaryKeyField} + "," +
<#list columns as column>
    <#if !column.isPrimaryKey>
        "${column.fieldName}=" + ${column.fieldName} + "," +
    </#if>
</#list>        "]";
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ${className} that = (${className}) o;
        return ${primaryKeyField} != null ? ${primaryKeyField}.equals(that.${primaryKeyField}) : that.${primaryKeyField} == null;
    }

    @Override
    public int hashCode() {
        return ${primaryKeyField} != null ? ${primaryKeyField}.hashCode() : 0;
    }
}