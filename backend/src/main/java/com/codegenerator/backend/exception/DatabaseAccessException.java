package com.codegenerator.backend.exception;

/**
 * 数据库访问异常类，用于处理数据库访问过程中的错误
 */
public class DatabaseAccessException extends RuntimeException {
    
    public DatabaseAccessException(String message) {
        super(message);
    }
    
    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}