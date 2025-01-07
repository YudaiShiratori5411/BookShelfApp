package com.example.bookshelf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "rakuten")
@Data
public class RakutenConfig {
    @Value("${rakuten.application-id}")
    private String applicationId;
    
    private Books books = new Books();

    @Data
    public static class Books {
        @Value("${rakuten.books.api-url}")
        private String apiUrl;
    }
}