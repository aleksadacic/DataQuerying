package io.github.aleksadacic.dataquerying.integration.test;

import io.github.aleksadacic.dataquerying.api.SearchRequest;
import io.github.aleksadacic.dataquerying.integration.config.TestConfig;
import io.github.aleksadacic.dataquerying.integration.model.Role;
import io.github.aleksadacic.dataquerying.integration.model.User;
import io.github.aleksadacic.dataquerying.integration.repository.RoleRepository;
import io.github.aleksadacic.dataquerying.integration.repository.UserRepository;
import io.github.aleksadacic.dataquerying.integration.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class SearchRequestIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Ensure you have this repository

    @BeforeEach
    void setUp() {
        // Initialize test data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("USER");
        roleRepository.save(userRole);

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
    }

    @Test
    void whenPageInfoIsSet_thenGetPageRequestReturnsCorrectPageable() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withPageInfo.json");

        Page<User> users = userRepository.findAll(searchRequest.getSpecification(), searchRequest.getPageRequest());

        assertThat(users).isEmpty(); // Page 1 should be empty.
        assertThat(users.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(users.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    void whenPageInfoIsNull_thenGetPageableReturnsUnpaged() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_noPageInfo.json");

        Page<User> users = userRepository.findAll(searchRequest.getSpecification(), searchRequest.getPageable());

        assertThat(users).hasSize(3);
        assertThat(users.getPageable()).isEqualTo(Pageable.unpaged());
    }

    @Test
    void whenPageSizeIsNull_thenGetPageableReturnsUnpaged() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_pageSizeNull.json");

        Page<User> users = userRepository.findAll(searchRequest.getSpecification(), searchRequest.getPageable());

        assertThat(users).hasSize(3);
        assertThat(users.getPageable()).isEqualTo(Pageable.unpaged());
    }

    @Test
    void whenPageInfoIsSet_thenGetPageRequestReturnsCorrectPageRequest() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withPageAndOrder.json");

        Page<User> usersPage = userRepository.findAll(searchRequest.getSpecification(), searchRequest.getPageable());

        // Assert
        assertThat(usersPage).isNotNull().hasSize(3);
        assertThat(usersPage.getPageable().getPageNumber()).isZero();
        assertThat(usersPage.getPageable().getPageSize()).isEqualTo(20);
        assertThat(usersPage.getPageable().getSort()).isEqualTo(Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("email")
        ));

        List<User> users = usersPage.getContent();
        // Assert the order of users
        assertThat(users)
                .extracting(User::getName, User::getEmail)
                .containsExactly(
                        tuple("Alice", "alice@example.com"),
                        tuple("Bob", "bob@example.com"),
                        tuple("Charlie", "charlie@example.com")
                );
    }

    @Test
    void whenPageInfoIsIncomplete_thenGetPageRequestReturnsNull() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_incompletePageInfo.json");

        Specification<User> specification = searchRequest.getSpecification();
        PageRequest pageRequest = searchRequest.getPageRequest();

        assertThrows(NullPointerException.class,
                () -> userRepository.findAll(specification, pageRequest));
    }

    @Test
    void whenOrderIsSet_thenGetSortReturnsCorrectSort() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withOrder.json");

        List<User> users = userRepository.findAll(searchRequest.getSpecification(), searchRequest.getSort());

        assertThat(users).hasSize(3);
        assertThat(users)
                .extracting(User::getName, User::getEmail)
                .containsExactly(
                        tuple("Alice", "alice@example.com"),
                        tuple("Bob", "bob@example.com"),
                        tuple("Charlie", "charlie@example.com")
                );
    }

    @Test
    void whenOrderIsEmpty_thenGetSortReturnsEmptySort() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_emptyOrder.json");

        Sort sort = searchRequest.getSort();

        assertThat(sort.isSorted()).isFalse();
    }

    @Test
    void whenSearchWithPageInfo_thenRetrieveCorrectPage() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withPageInfo.json");

        // Assuming no filters, retrieve paginated users
        List<User> users = userRepository.findAll(searchRequest.getSpecification(), searchRequest.getPageable()).getContent();

        // Page 1 should be empty, because page 0 contains only 3 users.
        assertThat(users).isEmpty();
    }

    @Test
    void whenSearchWithSort_thenRetrieveSortedUsers() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withOrder.json");

        List<User> users = userRepository.findAll(searchRequest.getSpecification(), searchRequest.getSort());

        // Expect users sorted by name ASC, then email DESC
        assertThat(users).extracting(User::getName)
                .containsExactly("Alice", "Bob", "Charlie");
    }

    @Test
    void whenSearchWithFilters_thenRetrieveFilteredUsers() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withFilters.json");

        List<User> users = userRepository.findAll(searchRequest.getSpecification());

        // Expect only Alice
        assertThat(users).hasSize(1)
                .extracting(User::getName)
                .containsExactly("Alice");
    }


    @Test
    void whenSearchWithMultipleFilters_thenRetrieveCorrectUsers() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withMultipleFilters.json");

        List<User> users = userRepository.findAll(searchRequest.getSpecification());

        // Expect only Bob (role=USER and name starts with "B")
        assertThat(users).hasSize(1)
                .extracting(User::getName)
                .containsExactly("Bob");
    }

    @Test
    void whenSearchWithOrCondition_thenRetrieveCorrectUsers() {
        SearchRequest searchRequest = JsonUtils.loadSearchRequestFromJson("searchRequest_withOrCondition.json");

        List<User> users = userRepository.findAll(searchRequest.getSpecification());

        // Expect Alice and Bob
        assertThat(users).hasSize(2)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }
}
