package com.example.travel.config; // 或您的配置包

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDirPath = Paths.get(uploadDir);
        String uploadPath = uploadDirPath.toFile().getAbsolutePath();


        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");


    }
}