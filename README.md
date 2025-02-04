![Data Querying](https://github.com/aleksadacic/DataQuerying/blob/master/.github/cover-image.png?raw=true)

# Data Querying

**Data Querying** is a lightweight and intuitive library designed to make dynamic query building in Spring Data
JPA
effortless.

Instead of manually crafting complex queries, this library provides a clean and type-safe DSL to construct JPA queries
at runtime. With objects like Query and SearchRequest, you can filter, sort, and paginate data dynamically, all while
keeping your code readable and maintainable.

<details>
<summary>❌ Traditional JPA Criteria API (Verbose & Complex)</summary>

```java
public class UserSpecification {

    public static Specification<User> hasName(String name) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                cb.equal(root.get("name"), name);
    }

    public static Specification<User> hasAgeGreaterThan(int age) {
        return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                cb.greaterThan(root.get("age"), age);
    }
}
```

And then:

```java
    Specification<User> spec = Specification
        .where(UserSpecification.hasName("John"))
        .and(UserSpecification.hasAgeGreaterThan(25));

List<User> users = userRepository.findAll(spec);
```

*Imagine doing that for every custom search!*

</details>

<details>
<summary>✅ Using a DSL (Simplified & Readable)</summary>

```java
Query<User> query = Query
        .where("name", SearchOperator.EQ, "John")
        .and("age", SearchOperator.GT, 25);

List<User> users = userRepository.findAll(query.buildSpecification());
```

</details>

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
    - [SearchOperator](#searchoperator)
    - [Query](#query)
    - [SearchRequest](#searchrequest)
- [Usage Examples](#usage-examples)
    - [Building Queries Directly](#building-queries-directly)
    - [Using SearchRequest in APIs](#using-searchrequest-in-apis)
- [Advanced Usage](#advanced-usage)
- [Exceptions](#exceptions)
- [Package Structure](#package-structure)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Dynamic Query Building**: Create complex JPA queries with an intuitive, fluent API.
- **Search request DTO** for your endpoints out of the box which you can convert to `Specification` object.
- **Nested Filters**: Build recursive AND/OR conditions.
- **Multiple Joins**: Deep join. Filter by your joined attribute (e.g. user.card.bank.bankName).
- **Minimal Boilerplate**: No need to hand-write every `Specification` --the library does it for you.
- **Projections** simplified. Just provide a POJO or an interface with getters in the `executeQuery` method for valid
  mapping.
- **Integration with Spring Data**: Seamlessly use `Specification`, `Pageable`, etc.

## Compatibility

This library is compatible with:

- **Spring Framework**: 6.2.2+
- **Spring Boot**: 3.4.2+
- **Java Version**: 17 - 23

For more compatibility details
visit [this page](https://github.com/aleksadacic/DataQuerying/blob/master/COMPATIBILITY.md).

## Installation

<details>
<summary>Maven</summary>

```xml

<dependency>
    <groupId>io.github.aleksadacic.dataquerying</groupId>
    <artifactId>DataQuerying</artifactId>
    <version>1.0.0</version>
</dependency>
```

</details>

[//]: # (TODO Replace **YOUR_VERSION** with the actual version once it's published to a Maven repository, e.g., Maven Central, JitPack, or your private repository)

## Quick Start

1. **Add dependency** to your Spring Boot or Spring application (see [Installation](#installation)).
2. **Use** `Query` or `SearchRequest` on your repository/service code to build dynamic queries.
3. **Integrate** the resulting `Specification<T>` or `criteriaCodeExpressions` related to your existing Spring Data JPA
   setup.

## Core Concepts

### SearchOperator

An ***enum*** defining different comparison operators (e.g., `EQ`, `NOT_EQ`, `GTE`, `LTE`, `IN`, `LIKE`, etc.) This is
part of the *public API* and can be referenced directly when building queries.

```java
public enum SearchOperator {
    EQ,
    NOT_EQ,
    GTE,
    LTE,
    GT,
    LT,
    IN,
    BETWEEN,
    LIKE,
    NOT_LIKE
}
```

### Query

A fluent builder for creating JPA Specification<T> objects or executing queries directly via JPA’s Criteria API.

Key methods:

- `where(...), and(...), or(...)` – attach conditions to the query.
- `join(...)` – specify join clauses (including nested joins).
- `distinct()` – force distinct selection if needed.
- `buildSpecification()` – produce a Specification<T> for Spring Data JPA usage.
- `executeQuery(...)` – if you want to manually run a Criteria query with an EntityManager.

Example:

```java
  Query<User> userQuery = Query.get()
        .where("email", SearchOperator.EQ, "test@example.com")
        .or("active", SearchOperator.EQ, true);

Specification<User> spec = userQuery.buildSpecification();
List<User> results = userRepository.findAll(spec);
```

### SearchRequest

A data structure for capturing search criteria in a JSON-friendly format. It contains:

- A list of FilterData (attribute, value, operator, nested filters).
- An overall logical operator (AND/OR) for the top-level filters.
- Pagination (PageInfo) and sorting (OrderInfo) metadata.

Typically, you’d receive a `SearchRequest` in a REST controller from the client side. Then you convert it to a `Query<>`
via `getQuery` and use it in your source code to modify the query you're building.

You can also just get
a `Specification<>` object easily by calling the
method `getSpecification` to pass to *repository* methods.

## Usage Examples

### Building Queries Directly

When you need a dynamic query in your service or repository layer:

```java
  Query<User> userQuery = Query.get()
        .where("country", SearchOperator.EQ, "USA")
        .and("age", SearchOperator.GTE, 18)
        .distinct();

Specification<User> spec = userQuery.buildSpecification();
List<User> adultsInUSA = userRepository.findAll(spec);
```

### Using SearchRequest in APIs

Create a `SearchRequest` on the client side (or in your code) that includes filters and pagination:

```json
{
  "filters": [
    {
      "attribute": "city",
      "searchOperator": "EQ",
      "value": "New York"
    },
    {
      "attribute": "active",
      "searchOperator": "EQ",
      "value": true
    }
  ],
  "conditionalOperator": "AND",
  "page": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "order": [
    {
      "attribute": "lastName",
      "sortOrder": "ASC"
    }
  ]
}

```

Receive it in your Spring REST Controller:

```java

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/search")
    public List<User> search(@RequestBody SearchRequest request) {
        return userRepository.findAll(request.getSpecification(), request.getPageRequest());
    }
}
```

Execute the query with your repository, taking advantage of the PageRequest or Sort from the SearchRequest.

## Advanced Usage

### Dynamically Enhancing a SearchRequest Before Execution

One of the powerful features of this library is the ability to dynamically modify a SearchRequest at runtime before
executing it. This is particularly useful when you need to apply additional filters or conditions based on business
logic, user roles, or other dynamic factors.

Example: Enhancing a SearchRequest in a Controller

Imagine you receive a `SearchRequest` from the client but want to programmatically add conditions such as ensuring the
user can only query their own organization, or adding global filters like "only active records." Here's how you can
enhance the request and execute the query:

```java

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/search")
    public List<User> enhancedSearch(@RequestBody SearchRequest request) {
        // Step 1: Convert SearchRequest to Query
        Query<User> query = request.getQuery();

        // Step 2: Add additional conditions programmatically
        query.and("status", SearchOperator.EQ, "ACTIVE"); // Global filter
        query.and("organizationId", SearchOperator.EQ, getCurrentUserOrganizationId()); // User-specific filter

        // Step 3: Build Specification and execute query
        Specification<User> spec = query.buildSpecification();
        return userRepository.findAll(spec, request.getPageRequest());
    }

    private Long getCurrentUserOrganizationId() {
        // Simulate fetching current user's organization ID
        return 123L;
    }
}
```

### Executing query with projection and pagination

This approach also supports complex query scenarios, such as adding join conditions dynamically. Then, you can retrieve
a list of projections as `List<Dto>` to return as a response:

```java

@PostMapping("/searchWithJoins")
public Page<UserDTO> searchWithJoins(@RequestBody SearchRequest request) {
    // Convert SearchRequest to Query
    Query<User> query = request.getQuery();

    // Add a join condition dynamically
    query = query.join("role", JoinType.INNER).and("role.name", SearchOperator.EQ, "ADMIN");

    return query.executeQuery(entityManager, User.class, UserDTO.class, request.getPageRequest());
}

```

> [!NOTE]
> Classes under `internal` package should only be used if you know and understand the
> internals. The library’s main public classes are `Query`, `SearchRequest`, and `SearchOperator`.

## Exceptions

When using the dynamic query features in this library, you may encounter the following exceptions. They are typically
thrown if your filter definitions or query parameters are invalid. Catch them where appropriate, or surface them as HTTP
errors in your REST APIs.

- `AttributeNotFoundException` (`SpecificationBuilderException`)  
  Thrown when the specified attribute does not exist on the entity when trying to create `Specification` object. For
  instance, if your query references a field that
  isn’t a valid column/property, this exception indicates the attribute cannot be resolved.


- `JoinNotFoundException` (`SpecificationBuilderException`)  
  Thrown when attempting to perform a `join` on a relationship that doesn't exist or isn’t properly mapped. This might
  happen if you reference a nested path (e.g., `user.address.city`) and one of the segments isn’t a valid association.


- `SpecificationBuilderException` (`RuntimeException`)  
  This is a wrapper for the `RuntimeException`. Thrown if there is an error in building the `Specification`—for example,
  conflicting operators, logical errors in
  filter groupings, or issues that prevent the library from creating a valid JPA criteria.

> [!NOTE]
> In most cases, these exceptions indicate an issue with how your filters or joins are configured. Proper validation of
> incoming request data (e.g., attribute names, operator types) can help avoid them.

## Package Structure

```
  io.github.aleksadacic.dataquerying
  ├── api
    ├── exceptions
  │ ├── Query.java // Public: dynamic query builder
  │ ├── SearchRequest.java // Public: JSON-friendly request object
  │ └── SearchOperator.java // Public: enumeration of operators (EQ, NOT_EQ, etc.)
  └── internal
    ├── enums
    ├── query
    ├── search
    └── utils
```

- Internal packages contain supporting classes (e.g., `SpecificationQuery`, `SpecificationEngine`) not intended for
  direct use.
- Only api classes are part of the stable, public-facing API.

## Contributing

Contributions are encouraged and accepted. To view more information about contribution
visit [this page](https://github.com/aleksadacic/DataQuerying/blob/master/CONTRIBUTING.md).

## License

This project is licensed under the [Apache-2.0 license](LICENSE.txt). Feel free to modify and distribute under these
terms.
