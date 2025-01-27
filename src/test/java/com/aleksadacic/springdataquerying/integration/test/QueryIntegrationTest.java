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
import jakarta.persistence.criteria.JoinType;
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
    void testExecuteQuerySimpleWhereCondition() {
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
    void testExecuteQueryAndCondition() {
        Query<User> query = Query.<User>where("email", "bob@example.com").and("name", "Bob");

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Assert that only Bob is returned
        assertThat(results).hasSize(1);
        UserDTO bobDTO = results.getFirst();
        assertThat(bobDTO.getName()).isEqualTo("Bob");
        assertThat(bobDTO.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testExecuteQueryOrCondition() {
        Query<User> query = Query.<User>where("name", "Alice").or("name", "Charlie");

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Assert that Alice and Charlie are returned
        assertThat(results).hasSize(2);
        assertThat(results).extracting(UserDTO::getName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void testExecuteQueryJoinCondition() {
        Query<User> query = Query.<User>get()
                .and("role.name", "ADMIN");

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        UserDTO aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
    }

    @Test
    void testExecuteQueryDistinct() {
        // Suppose there are multiple users with the same role
        // Let's query distinct roles
        Query<User> query = Query.<User>get().distinct().join("role", JoinType.INNER);

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);
        assertThat(results).hasSize(3);
    }

    @Test
    void testExecuteQueryComplexQuery() {
        Query<User> query = Query.<User>get()
                .join("role", JoinType.INNER)
                .and("role.name", "ADMIN")
                .and(Query.<User>get().or("name", "Alice").or("name", "Bob"));

        List<UserDTO> results = query.executeQuery(entityManager, User.class, UserDTO.class);

        // Only Alice should match
        assertThat(results).hasSize(1);
        UserDTO aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
    }

    @Test
    void testRepositoryFetchSimpleWhereCondition() {
        Query<User> query = Query.where("name", "Alice");

        // Use the repository to fetch data using the query
        List<User> results = userRepository.findAll(query.buildSpecification());

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        User alice = results.getFirst();
        assertThat(alice.getName()).isEqualTo("Alice");
        assertThat(alice.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void testRepositoryFetchAndCondition() {
        Query<User> query = Query.<User>where("email", "bob@example.com").and("name", "Bob");

        // Use the repository to fetch data using the query
        List<User> results = userRepository.findAll(query.buildSpecification());

        // Assert that only Bob is returned
        assertThat(results).hasSize(1);
        User bob = results.getFirst();
        assertThat(bob.getName()).isEqualTo("Bob");
        assertThat(bob.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testRepositoryFetchOrCondition() {
        Query<User> query = Query.<User>where("name", "Alice").or("name", "Charlie");

        // Use the repository to fetch data using the query
        List<User> results = userRepository.findAll(query.buildSpecification());

        // Assert that Alice and Charlie are returned
        assertThat(results).hasSize(2);
        assertThat(results).extracting(User::getName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void testRepositoryFetchJoinCondition() {
        Query<User> query = Query.<User>get().join("role", JoinType.INNER).and("role.name", "ADMIN");

        // Use the repository to fetch data using the query
        List<User> results = userRepository.findAll(query.buildSpecification());

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        User alice = results.getFirst();
        assertThat(alice.getName()).isEqualTo("Alice");
        assertThat(alice.getRole().getName()).isEqualTo("ADMIN");
    }

    @Test
    void testRepositoryFetchComplexQuery() {
        Query<User> query = Query.<User>get()
                .join("role", JoinType.INNER)
                .and("role.name", "USER")
                .and(Query.<User>get().or("name", "Alice").or("name", "Bob"));

        // Use the repository to fetch data using the query
        List<User> results = userRepository.findAll(query.buildSpecification());

        // Assert that only Bob matches the conditions
        assertThat(results).hasSize(1);
        User bob = results.getFirst();
        assertThat(bob.getName()).isEqualTo("Bob");
        assertThat(bob.getRole().getName()).isEqualTo("USER");
    }

    @Test
    void testRepositoryFetchDistinct() {
        // Build a query to find distinct users based on roles
        Query<User> query = Query.<User>get().distinct().join("role", JoinType.INNER);

        // Use the repository to fetch data using the query
        List<User> results = userRepository.findAll(query.buildSpecification());

        // Assert that all users are returned (distinct by role)
        assertThat(results).hasSize(3);
        assertThat(results).extracting(User::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
    }

}
