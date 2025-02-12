package io.github.aleksadacic.dataquerying.integration.test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.aleksadacic.dataquerying.api.Projection;
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
import org.springframework.data.jpa.domain.Specification;
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
    void testGetFromSpecification() {
        // Create a specification that filters users with name "Alice"
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("name"), "Alice");

        // Create a new Query using the factory method with Specification
        Query<User> queryFromSpec = Query.get(spec);

        List<User> results = userRepository.findAll(queryFromSpec.buildSpecification());

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Alice");
    }

    @Test
    void testGetFromExistingQuery() {
        // Create an initial Query filtering for users with email "bob@example.com"
        Query<User> initialQuery = Query.where("email", "bob@example.com");

        // Create a new Query using the factory method based on an existing query
        Query<User> queryFromExisting = Query.get(initialQuery);

        List<User> results = userRepository.findAll(queryFromExisting.buildSpecification());

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Bob");
    }

    @Test
    void testProjectionWithSimpleWhereCondition() {
        // Build a query to find users with name 'Alice'
        Query<User> query = Query.where("name", "Alice");

        List<UserDto> results = Projection.create(entityManager, User.class, UserDto.class).findAll(query);

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        UserDto aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
        assertThat(aliceDTO.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void testProjectionAndCondition() {
        Query<User> query = Query.<User>where("email", "bob@example.com").and("name", "Bob");

        List<UserDto> results = Projection.create(entityManager, User.class, UserDto.class).findAll(query);

        // Assert that only Bob is returned
        assertThat(results).hasSize(1);
        UserDto bobDTO = results.getFirst();
        assertThat(bobDTO.getName()).isEqualTo("Bob");
        assertThat(bobDTO.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testProjectionOrCondition() {
        Query<User> query = Query.<User>where("name", "Alice").or("name", "Charlie");

        List<UserDto> results = Projection.create(entityManager, User.class, UserDto.class).findAll(query);

        // Assert that Alice and Charlie are returned
        assertThat(results).hasSize(2);
        assertThat(results).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("Alice", "Charlie");
    }

    @Test
    void testProjectionJoinCondition() {
        Query<User> query = Query.<User>get()
                .and("role.name", "ADMIN");

        List<UserDto> results = Projection.create(entityManager, User.class, UserDto.class).findAll(query);

        // Assert that only Alice is returned
        assertThat(results).hasSize(1);
        UserDto aliceDTO = results.getFirst();
        assertThat(aliceDTO.getName()).isEqualTo("Alice");
    }

    @Test
    void testProjectionDistinct() {
        // Suppose there are multiple users with the same role
        Query<User> query = Query.<User>get().distinct().join("role", JoinType.INNER);

        List<UserDto> results = Projection.create(entityManager, User.class, UserDto.class).findAll(query);
        assertThat(results).hasSize(3);
    }

    @Test
    void testProjectionComplexQuery() {
        Query<User> query = Query.<User>get()
                .join("role", JoinType.INNER)
                .and("role.name", "ADMIN")
                .and(Query.<User>get().or("name", "Alice").or("name", "Bob"));

        List<UserDto> results = Projection.create(entityManager, User.class, UserDto.class).findAll(query);

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
    void testProjectionWithPaginationAndSorting() {
        Query<User> query = Query.where("role.name", "USER");

        // Create a PageRequest with sorting
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("email")
        ));

        // Execute the query with pagination
        Page<UserDto> pageResult = Projection.create(entityManager, User.class, UserDto.class).findAll(query, pageRequest);

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
    void testProjectionWithMultiplePages() {
        Query<User> query = Query.where("role.name", "USER");

        // Create a PageRequest for the second page
        PageRequest pageRequest = PageRequest.of(1, 1, Sort.by(Sort.Order.asc("name")));

        // Execute the query with pagination
        Page<UserDto> pageResult = Projection.create(entityManager, User.class, UserDto.class).findAll(query, pageRequest);

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
    void testProjectionWithMultipleConditions() {
        Query<User> query = Query.<User>where("name", "Bob").and("superuser", true);

        // Execute the query with pagination
        List<UserDto> result = Projection.create(entityManager, User.class, UserDto.class).findAll(query);

        // Assert page metadata
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).extracting(UserDto::getName).containsExactly("Bob");
        assertThat(result).extracting(UserDto::isSuperuser).containsExactly(true);
    }

    @Test
    void testProjectionWithMultipleConditions_interfaceDto() {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
        interface X {
            String getName();

            boolean getSuperuser();
        }

        Query<User> query = Query.<User>where("name", "Bob").and("superuser", true);

        // Execute the query with pagination
        List<X> result = Projection.create(entityManager, User.class, X.class).findAll(query);

        // Assert page metadata
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).extracting(X::getName).containsExactly("Bob");
        assertThat(result).extracting(X::getSuperuser).containsExactly(true);
    }

    @Test
    void testProjectionWithComplexConditionsAndPagination() {
        Query<User> query = Query.<User>get()
                .join("role", JoinType.INNER)
                .and("role.name", "USER")
                .and(Query.<User>get().or("name", "Bob").or("name", "Charlie"));

        // Create a PageRequest with a page size of 1
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("name")));

        // Execute the query with pagination
        Page<UserDto> pageResult = Projection.create(entityManager, User.class, UserDto.class).findAll(query, pageRequest);

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
        Page<UserDto> secondPageResult = Projection.create(entityManager, User.class, UserDto.class).findAll(query, pageRequest.next());
        List<UserDto> secondContent = secondPageResult.getContent();

        assertThat(secondContent).hasSize(1);
        assertThat(secondContent).extracting(UserDto::getName)
                .containsExactly("Bob");
    }
}
