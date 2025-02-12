package io.github.aleksadacic.dataquerying.integration.test;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class ProjectionIntegrationTest {
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
    void testProjectionFindAllUsingSpecificationFactoryMethod() {
        // Manually create a specification filtering for role "ADMIN"
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("role").get("name"), "ADMIN");
        // Use the Projection factory method with the specification
        List<UserDto> dtos = Projection.create(entityManager, User.class, UserDto.class)
                .findAll(spec, Sort.by("name"), false);
        // Expect only the user with role "ADMIN" (i.e. Alice)
        assertThat(dtos).hasSize(1);
        assertThat(dtos.getFirst().getName()).isEqualTo("Alice");
    }

    @Test
    void testProjectionFindAllUsingExistingQueryFactoryMethod() {
        // Create an initial query filtering for users with email "bob@example.com"
        Query<User> initialQuery = Query.where("email", "bob@example.com");
        // Use the factory method to create a new query based on the existing one
        Query<User> queryFromExisting = Query.get(initialQuery);
        // Use Projection with the new query (with sorting and distinct true)
        List<UserDto> dtos = Projection.create(entityManager, User.class, UserDto.class)
                .findAll(queryFromExisting, Sort.by("name"), true);
        // Expect only Bob to be returned
        assertThat(dtos).hasSize(1);
        assertThat(dtos.getFirst().getName()).isEqualTo("Bob");
    }
}
