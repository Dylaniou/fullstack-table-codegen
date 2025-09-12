package com.codegenerator.backend.repository;

import com.codegenerator.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * User 数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // 搜索方法 - 用于分页查询
    @Query("SELECT e FROM User e WHERE CONCAT(e.id, '') LIKE %:search%"
                           + " OR e.mobile LIKE %:search%"
                           + " OR e.passwd LIKE %:search%"
                           + " OR e.name LIKE %:search%"
                           + " OR e.sex LIKE %:search%"
                           + " OR e.age LIKE %:search%"
                           + " OR e.area LIKE %:search%"
           )
    Page<User> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    // 搜索方法 - 用于Excel导出
    @Query("SELECT e.id, e.mobile, e.passwd, e.name, e.sex, e.age, e.birthday, e.area, e.score FROM User e WHERE CONCAT(e.id, '') LIKE %:search%"
                           + " OR e.mobile LIKE %:search%"
                           + " OR e.passwd LIKE %:search%"
                           + " OR e.name LIKE %:search%"
                           + " OR e.sex LIKE %:search%"
                           + " OR e.age LIKE %:search%"
                           + " OR e.area LIKE %:search%"
           )
    List<User> findBySearchTerm(@Param("search") String search);
}