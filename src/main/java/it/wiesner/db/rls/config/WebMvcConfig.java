package it.wiesner.db.rls.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.wiesner.db.rls.interceptor.RlsSessionInterceptor;

/**
 * Web MVC configuration for registering interceptors.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RlsSessionInterceptor rlsSessionInterceptor;

    @SuppressWarnings("null")
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the RLS session interceptor for all requests
        registry.addInterceptor(rlsSessionInterceptor)
                .addPathPatterns("/rls/**");  // Apply to all RLS endpoints
    }
}
