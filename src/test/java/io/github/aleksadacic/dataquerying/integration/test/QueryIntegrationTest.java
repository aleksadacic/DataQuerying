package io.github.aleksadacic.dataquerying.integration.test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.aleksadacic.dataquerying.api.Query;
import io.github.aleksadacic.dataquerying.integration.config.TestConfig;
import io.github.aleksadacic.dataquerying.integration.dto.UserDto;
import io.github.aleksadacic.dataquerying.integration.model.Role;
import io.github.aleksadacic.dataquerying.integration.model.User;
import io.github.aleksadacic.dataquerying.integration.repository.RoleRepository;
import io.github.aleksadacic.dataquerying.integration.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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
        bob.setSuperuser(true);
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

        List<UserDto> results = query.executeQuery(entityManager, User.class, UserDto.class);

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        UserDto aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
        assertThat(aliceDTO.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void testExecuteQueryAndCondition() {
        Query<User> query = Query.<User>where("email", "bob@example.com").and("name", "Bob");

        List<UserDto> results = query.executeQuery(entityManager, User.class, UserDto.class);

        // Assert that only Bob is returned
        assertThat(results).hasSize(1);
        UserDto bobDTO = results.getFirst();
        assertThat(bobDTO.getName()).isEqualTo("Bob");
        assertThat(bobDTO.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testExecuteQueryOrCondition() {
        Query<User> query = Query.<User>where("name", "Alice").or("name", "Charlie");

        List<UserDto> results = query.executeQuery(entityManager, User.class, UserDto.class);

        // Assert that Alice and Charlie are returned
        assertThat(results).hasSize(2);
        assertThat(results).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void testExecuteQueryJoinCondition() {
        Query<User> query = Query.<User>get()
                .and("role.name", "ADMIN");

        List<UserDto> results = query.executeQuery(entityManager, User.class, UserDto.class);

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        UserDto aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
    }

    @Test
    void testExecuteQueryDistinct() {
        // Suppose there are multiple users with the same role
        // Let's query distinct roles
        Query<User> query = Query.<User>get().distinct().join("role", JoinType.INNER);

        List<UserDto> results = query.executeQuery(entityManager, User.class, UserDto.class);
        assertThat(results).hasSize(3);
    }

    @Test
    void testExecuteQueryComplexQuery() {
        Query<User> query = Query.<User>get()
                .join("role", JoinType.INNER)
                .and("role.name", "ADMIN")
                .and(Query.<User>get().or("name", "Alice").or("name", "Bob"));

        List<UserDto> results = query.executeQuery(entityManager, User.class, UserDto.class);

        // Only Alice should match
        assertThat(results).hasSize(1);
        UserDto aliceDTO = results.getFirst();
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

    @Test
    void testExecuteQueryWithPaginationAndSorting() {
        Query<User> query = Query.where("role.name", "USER");

        // Create a PageRequest with sorting
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("email")
        ));

        // Execute the query with pagination
        Page<UserDto> pageResult = query.executeQuery(entityManager, User.class, UserDto.class, pageRequest);

        // Assert page metadata
        assertThat(pageResult).isNotNull();
        assertThat(pageResult.getTotalElements()).isEqualTo(2); // Total elements on this page
        assertThat(pageResult.getTotalPages()).isEqualTo(2); // Total pages
        assertThat(pageResult.getNumber()).isZero(); // Current page
        assertThat(pageResult.getSize()).isEqualTo(1); // Page size

        // Assert the content order
        List<UserDto> content = pageResult.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting(UserDto::getName, UserDto::getEmail)
                .containsExactly(
                        tuple("Bob", "bob@example.com")
                );
    }

    @Test
    void testExecuteQueryWithMultiplePages() {
        Query<User> query = Query.where("role.name", "USER");

        // Create a PageRequest for the second page
        PageRequest pageRequest = PageRequest.of(1, 1, Sort.by(Sort.Order.asc("name")));

        // Execute the query with pagination
        Page<UserDto> pageResult = query.executeQuery(entityManager, User.class, UserDto.class, pageRequest);

        // Assert page metadata
        assertThat(pageResult).isNotNull();
        assertThat(pageResult.getTotalElements()).isEqualTo(2); // Total elements matching "USER" role
        assertThat(pageResult.getTotalPages()).isEqualTo(2); // Total pages
        assertThat(pageResult.getNumber()).isEqualTo(1); // Current page
        assertThat(pageResult.getSize()).isEqualTo(1); // Page size

        // Assert the content on the second page
        List<UserDto> content = pageResult.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting(UserDto::getName)
                .containsExactly("Charlie");
    }

    @Test
    void testExecuteQueryWithMultipleConditions() {
        Query<User> query = Query.<User>where("name", "Bob").and("superuser", true);

        // Execute the query with pagination
        List<UserDto> result = query.executeQuery(entityManager, User.class, UserDto.class);

        // Assert page metadata
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).extracting(UserDto::getName).containsExactly("Bob");
        assertThat(result).extracting(UserDto::isSuperuser).containsExactly(true);
    }

    @Test
    void testExecuteQueryWithMultipleConditions_interfaceDto() {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
        interface X {
            String getName();

            boolean getSuperuser();
        }

        Query<User> query = Query.<User>where("name", "Bob").and("superuser", true);

        // Execute the query with pagination
        List<X> result = query.executeQuery(entityManager, User.class, X.class);

        // Assert page metadata
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).extracting(X::getName).containsExactly("Bob");
        assertThat(result).extracting(X::getSuperuser).containsExactly(true);
    }

    @Test
    void testExecuteQueryWithComplexConditionsAndPagination() {
        Query<User> query = Query.<User>get()
                .join("role", JoinType.INNER)
                .and("role.name", "USER")
                .and(Query.<User>get().or("name", "Bob").or("name", "Charlie"));

        // Create a PageRequest with a page size of 1
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("name")));

        // Execute the query with pagination
        Page<UserDto> pageResult = query.executeQuery(entityManager, User.class, UserDto.class, pageRequest);

        // Assert page metadata
        assertThat(pageResult).isNotNull();
        assertThat(pageResult.getTotalElements()).isEqualTo(2); // Total elements
        assertThat(pageResult.getTotalPages()).isEqualTo(2); // Total pages
        assertThat(pageResult.getNumber()).isZero(); // Current page
        assertThat(pageResult.getSize()).isEqualTo(1); // Page size

        // Assert the content on the first page (descending order by name)
        List<UserDto> content = pageResult.getContent();
        assertThat(content).hasSize(1);
        assertThat(content).extracting(UserDto::getName)
                .containsExactly("Charlie");

        // Execute for the second page
        Page<UserDto> secondPageResult = query.executeQuery(entityManager, User.class, UserDto.class, pageRequest.next());
        List<UserDto> secondContent = secondPageResult.getContent();

        assertThat(secondContent).hasSize(1);
        assertThat(secondContent).extracting(UserDto::getName)
                .containsExactly("Bob");
    }
}
