package com.curtisnewbie.service.chat.config;

import com.curtisnewbie.common.converters.EpochDateLongConverter;
import com.curtisnewbie.common.converters.EpochDateStringConverter;
import com.curtisnewbie.common.converters.EpochLongDateConverter;
import com.curtisnewbie.common.converters.EpochStringDateConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Configuration of Web MVC
 *
 * @author yongjie.zhuang
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final int RESOURCES_CACHE_MAX_AGE_MINUTES = 10;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // cache 10 minutes for the static resources
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(RESOURCES_CACHE_MAX_AGE_MINUTES, TimeUnit.MINUTES));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TracingHandlerInterceptor());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new EpochDateLongConverter());
        registry.addConverter(new EpochLongDateConverter());
        registry.addConverter(new EpochDateStringConverter());
        registry.addConverter(new EpochStringDateConverter());
    }

}
