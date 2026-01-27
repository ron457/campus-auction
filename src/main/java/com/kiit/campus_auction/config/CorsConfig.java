package com.kiit.campus_auction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "https://campus-auction-kiit.netlify.app/",  // ✅ Production Netlify
                    "http://localhost:5500",                        // ✅ Local dev
                    "http://127.0.0.1:5500"                        // ✅ Local dev alternative
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders(
                    "Origin",
                    "Content-Type",
                    "Accept",
                    "Authorization",
                    "X-Requested-With"
                )
                .exposedHeaders(
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials"
                )
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight for 1 hour
    }
}
