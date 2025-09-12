package com.codegenerator.backend.exception;

/**
 * 代码生成异常类，用于处理代码生成过程中的错误
 */
public class CodeGenerationException extends RuntimeException {
    
    public CodeGenerationException(String message) {
        super(message);
    }
    
    public CodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}