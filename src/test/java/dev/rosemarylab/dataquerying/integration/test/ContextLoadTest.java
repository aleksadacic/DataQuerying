package dev.rosemarylab.dataquerying.integration.test;

import dev.rosemarylab.dataquerying.api.ProjectionFactory;
import dev.rosemarylab.dataquerying.integration.config.TestConfig;
import dev.rosemarylab.dataquerying.integration.repository.RoleRepository;
import dev.rosemarylab.dataquerying.integration.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class ContextLoadTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JpaTransactionManager transactionManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectionFactory projectionFactory;

    @Test
    void contextLoads() {
        assertNotNull(dataSource);
        assertNotNull(entityManagerFactory);
        assertNotNull(transactionManager);
        assertNotNull(userRepository);
        assertNotNull(roleRepository);
        assertNotNull(projectionFactory);
    }
}
