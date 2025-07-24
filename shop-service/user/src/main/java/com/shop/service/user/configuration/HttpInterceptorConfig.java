package com.shop.service.user.configuration;


import com.shop.service.module.feign.LogFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class HttpInterceptorConfig implements WebMvcConfigurer {


    @Autowired
    private LogFeignService logFeignService;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册TestInterceptor拦截器
        InterceptorRegistration registration = registry.addInterceptor(new GlobalLogInterceptorHandler(logFeignService));
        registration.addPathPatterns("/**");//所有路径都被拦截

    }
}
