package com.cts.mfrp.pc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Files may be stored relative to the project root (one level above pc/)
        // Support both locations so uploads work regardless of working directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/", "file:../uploads/");
    }
}
