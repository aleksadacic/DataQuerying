package dev.rosemarylab.dataquerying.config;

import dev.rosemarylab.dataquerying.api.ProjectionFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataQueryingAutoConfiguration {
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public ProjectionFactory projectionFactory() {
        return new ProjectionFactory(entityManager);
    }
}
