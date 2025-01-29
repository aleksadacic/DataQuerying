package com.aleksadacic.dataquerying.integration.test;

import com.aleksadacic.dataquerying.integration.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class ContextLoadTest {

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
