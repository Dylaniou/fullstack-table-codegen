package ${repositoryPackage};

import ${modelPackage}.${className};
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * ${className} 数据访问层
 */
@Repository
public interface ${className}Repository extends JpaRepository<${className}, ${idType}> {
    
    // 搜索方法 - 用于分页查询
    @Query("SELECT e FROM ${className} e WHERE CONCAT(e.id, '') LIKE %:search%"
           <#if columns?? && (columns?size != 0)>
               <#list columns as col>
                   <#if col.field != "id">
                       <#if col.javaType == "String">
                           + " OR e.${col.field} LIKE %:search%"
                       <#elseif col.javaType == "Integer" || col.javaType == "Long">
                           + " OR CONCAT(e.${col.field}, '') LIKE %:search%"
                       </#if>
                   </#if>
               </#list>
           </#if>)
    Page<${className}> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    // 搜索方法 - 用于Excel导出
    @Query("SELECT <#if columns?? && (columns?size != 0)><#list columns as col>e.${col.field}<#if col_has_next>, </#if></#list><#else>e</#if> FROM ${className} e WHERE CONCAT(e.id, '') LIKE %:search%"
           <#if columns?? && (columns?size != 0)>
               <#list columns as col>
                   <#if col.field != "id">
                       <#if col.javaType == "String">
                           + " OR e.${col.field} LIKE %:search%"
                       <#elseif col.javaType == "Integer" || col.javaType == "Long">
                           + " OR CONCAT(e.${col.field}, '') LIKE %:search%"
                       </#if>
                   </#if>
               </#list>
           </#if>)
    List<${className}> findBySearchTerm(@Param("search") String search);
}