package com.aleksadacic.springdataquerying.integration.test;

import com.aleksadacic.springdataquerying.api.Query;
import com.aleksadacic.springdataquerying.integration.config.TestConfig;
import com.aleksadacic.springdataquerying.integration.dto.UserDTO;
import com.aleksadacic.springdataquerying.integration.model.Role;
import com.aleksadacic.springdataquerying.integration.model.User;
import com.aleksadacic.springdataquerying.integration.repository.RoleRepository;
import com.aleksadacic.springdataquerying.integration.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class QueryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        // Clear existing data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("USER");
        roleRepository.save(userRole);

        // Create users
        User alice = new User();
        alice.setName("Alice");
        alice.setEmail("alice@example.com");
        alice.setRole(adminRole);
        userRepository.save(alice);

        User bob = new User();
        bob.setName("Bob");
        bob.setEmail("bob@example.com");
        bob.setRole(userRole);
        userRepository.save(bob);

        User charlie = new User();
        charlie.setName("Charlie");
        charlie.setEmail("charlie@example.com");
        charlie.setRole(userRole);
        userRepository.save(charlie);

        // Flush to ensure data is persisted before tests
        entityManager.flush();
    }

    @Test
    void testSimpleWhereCondition() {
        // Build a query to find users with name 'Alice'
        Query<User> query = Query.where("name", "Alice");

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        UserDTO aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
        assertThat(aliceDTO.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void testAndCondition() {
        // Build a query to find users with role 'USER' and name 'Bob'
        Query<User> query = Query.<User>where("role.name", "USER")
                .and("name", "Bob");

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Assert that only Bob is returned
        assertThat(results).hasSize(1);
        UserDTO bobDTO = results.getFirst();
        assertThat(bobDTO.getName()).isEqualTo("Bob");
        assertThat(bobDTO.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testOrCondition() {
        // Build a query to find users with name 'Alice' or 'Charlie'
        Query<User> query = Query.<User>where("name", "Alice")
                .or("name", "Charlie");

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Assert that Alice and Charlie are returned
        assertThat(results).hasSize(2);
        assertThat(results).extracting(UserDTO::getName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void testJoinCondition() {
        // Build a query to find users with role 'ADMIN'
        Query<User> query = Query.<User>get()
                .join("role", jakarta.persistence.criteria.JoinType.INNER)
                .and("role.name", "ADMIN");

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        UserDTO aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
    }

    @Test
    void testDistinct() {
        // Suppose there are multiple users with the same role
        // Let's query distinct roles
        Query<User> query = Query.<User>get()
                .distinct()
                .join("role", jakarta.persistence.criteria.JoinType.INNER);

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Since distinct is applied, the number of roles should be unique
        // Here, ADMIN and USER, so 2 distinct roles
        assertThat(results).hasSize(2);
    }

    @Test
    void testComplexQuery() {
        // Build a complex query: (name = 'Alice' OR name = 'Bob') AND role = 'ADMIN'
        Query<User> query = Query.<User>where("role.name", "ADMIN")
                .and(Query.<User>get().or("name", "Alice")
                        .or("name", "Bob"));

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Only Alice should match
        assertThat(results).hasSize(1);
        UserDTO aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
    }
}
