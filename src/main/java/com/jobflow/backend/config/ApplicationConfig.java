package com.jobflow.backend.config;

import com.jobflow.backend.application.ApplicationStatusPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    ApplicationStatusPolicy applicationStatusPolicy() {
        return new ApplicationStatusPolicy();
    }
}
