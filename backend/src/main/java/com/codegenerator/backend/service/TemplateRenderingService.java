package com.codegenerator.backend.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * 模板渲染服务类，负责渲染FreeMarker模板
 */
@Service
public class TemplateRenderingService {

    @Autowired
    private Configuration freemarkerConfig;

    /**
     * 渲染模板到指定的Writer
     * @param templateName 模板名称
     * @param data 模板数据
     * @param writer 输出Writer
     * @throws IOException IO异常
     * @throws TemplateException 模板异常
     */
    public void renderTemplate(String templateName, Map<String, Object> data, Writer writer) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate(templateName);
        template.process(data, writer);
    }
}