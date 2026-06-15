package com.jobflow.backend.config;

import com.jobflow.backend.application.ApplicationStatusPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    ApplicationStatusPolicy applicationStatusPolicy() {
        return new ApplicationStatusPolicy();
    }

    @Bean
    TransactionOperations transactionOperations(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
