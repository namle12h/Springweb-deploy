package com.spring.Springweb.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CloudinaryConfig {

    @Value("${CLOUDINARY_CLOUD_NAME}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY}")
    private String apiKey;

    @Value("${CLOUDINARY_API_SECRET}")
    private String apiSecret;

    @Bean
    public com.cloudinary.Cloudinary cloudinary() {
        
        Map<String, String> config = Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        );
        return new com.cloudinary.Cloudinary(config);
    }
}
