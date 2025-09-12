package com.codegenerator.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器，用于统一处理所有异常
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    /**
     * 处理代码生成异常
     */
    @ExceptionHandler(CodeGenerationException.class)
    public ResponseEntity<?> handleCodeGenerationException(CodeGenerationException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("message", ex.getMessage());
        body.put("details", request.getDescription(false));
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DatabaseAccessException.class)
    public ResponseEntity<?> handleDatabaseAccessException(DatabaseAccessException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("message", ex.getMessage());
        body.put("details", request.getDescription(false));
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理其他所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("message", "系统发生错误: " + ex.getMessage());
        body.put("details", request.getDescription(false));
        
        ex.printStackTrace();
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}