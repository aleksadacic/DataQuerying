package com.aleksadacic.springdataquerying.config;

import com.aleksadacic.springdataquerying.api.Query;
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
