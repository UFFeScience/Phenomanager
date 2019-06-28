package com.uff.phenomanager.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.google.common.collect.ImmutableList;
import com.uff.phenomanager.Constants.JWT_AUTH;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(ImmutableList.of(JWT_AUTH.ALL_PATH_ORIGIN_REGEX));
        configuration.setAllowedMethods(ImmutableList.of(
        		HttpMethod.HEAD.name(), 
        		HttpMethod.OPTIONS.name(), 
        		HttpMethod.GET.name(), 
        		HttpMethod.PUT.name(), 
        		HttpMethod.POST.name(), 
        		HttpMethod.DELETE.name(), 
        		HttpMethod.PATCH.name()));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader(JWT_AUTH.CONTENT_DISPOSITION);
        configuration.setAllowedHeaders(ImmutableList.of(
        		JWT_AUTH.AUTHORIZATION, 
        		JWT_AUTH.CACHE_CONTROL, 
        		JWT_AUTH.CONTENT_TYPE,
        		JWT_AUTH.X_ACCESS_TOKEN));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(JWT_AUTH.ALL_PATH_CORS_REGEX, configuration);
        
        return source;
    }
    
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
}