package io.github.aleksadacic.dataquerying.config;

import io.github.aleksadacic.dataquerying.api.Query;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Option to use dependency injection to autowire the <code>Query</code> object by declaring it as a <code>@Bean</code>.
 */
@Configuration
public class QueryConfiguration {
    @Bean
    public <T> Query<T> query() {
        return Query.get();
    }
}
