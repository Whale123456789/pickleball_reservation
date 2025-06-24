// RepositoryConfig.java
package com.pickleball_backend.pickleball.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.pickleball_backend.pickleball.repository",
        entityManagerFactoryRef = "entityManagerFactory"
)
public class RepositoryConfig {
}