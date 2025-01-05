package com.example.bookshelf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.bookshelf.repository")
@EnableTransactionManagement
public class BookShelfAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookShelfAppApplication.class, args);
    }
}