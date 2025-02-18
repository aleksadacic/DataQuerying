![Data Querying](https://github.com/aleksadacic/DataQuerying/blob/master/.github/cover-image.png?raw=true)

# Data Querying

**Data Querying** is a lightweight and intuitive library designed to make **dynamic query building**
in Spring Data JPA **effortless**.

Instead of manually crafting complex queries,
this library provides a **clean and type-safe DSL** to construct JPA queries at runtime.
With objects like `Query` and `SearchRequest`, you can **filter, sort, and paginate data dynamically**,
all while keeping your code **readable and maintainable**.

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
- [Compatibility](#compatibility)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
    - [SearchOperator](#searchoperator)
    - [Query](#query)
    - [SearchRequest](#searchrequest)
    - [Projection](#projection)
- [Usage Examples](#usage-examples)
    - [Building Queries Directly](#building-queries-directly)
    - [Using SearchRequest in APIs](#using-searchrequest-in-apis)
    - [In combination with Projection](#in-combination-with-projection)
- [Advanced Usage](#advanced-usage)
- [Exceptions](#exceptions)
- [Package Structure](#package-structure)
- [Contributing](#contributing)
- [License](#license)

## Features

* **Dynamic Query Building**: Create complex JPA queries with an intuitive, fluent API.
* **Search request DTO** for your endpoints out of the box, which you can convert to a `Specification` object.
* **Nested Filters**: Build recursive AND/OR conditions.
* **Multiple Joins**: Deep join support to filter by joined attributes (e.g., `user.card.bank.bankName`).
* **Minimal Boilerplate**: No need to manually write `Specification` implementations.
* **Projection Interface Support**: Easily map query results to DTOs or interfaces.
* **Integration with Spring Data**: Seamlessly use `Specification`, `Pageable`, and `Sort`.

## Compatibility

This library is compatible with:

- **Spring Framework**: 6.2.2+
- **Spring Boot**: 3.4.2+
- **Java Version**: 17 - 23

For more compatibility details
visit [COMPATIBILITY.md](https://github.com/aleksadacic/DataQuerying/blob/master/COMPATIBILITY.md).

## Installation

<details>
<summary>Maven</summary>

```xml

<dependency>
    <groupId>dev.rosemarylab.dataquerying</groupId>
    <artifactId>DataQuerying</artifactId>
    <version>2.0.0</version>
</dependency>
```

</details>

## Quick Start

1. **Add dependency** to your Spring Boot or Spring application (see [Installation](#installation)).
2. **Use** `Query` or `SearchRequest` on your repository/service code to build dynamic queries.
3. **Integrate** the resulting `Specification<T>` into your existing Spring Data JPA setup.

## Core Concepts

### SearchOperator

An ***enum*** defining different comparison operators (e.g., `EQ`, `NOT_EQ`, `GTE`, `LTE`, `IN`, `LIKE`, etc.) This is
part of the *public API* and can be referenced directly when building queries.

```java
public enum SearchOperator {
    EQ, NOT_EQ, GTE, LTE, GT, LT, IN, BETWEEN, LIKE, NOT_LIKE
}
```

### Query

A fluent builder for creating JPA `Specification<T>` objects.

```java
Query<User> userQuery = Query.get()
        .where("email", SearchOperator.EQ, "test@example.com")
        .or("active", SearchOperator.EQ, true);

Specification<User> spec = userQuery.buildSpecification();
List<User> results = userRepository.findAll(spec);
```

### SearchRequest

A JSON-friendly structure for capturing search criteria, including pagination and sorting.

Typically, you’d receive a `SearchRequest` in a REST controller from the client side. Then you convert it to a `Query<>`
via `getQuery` and use it in your source code to modify the query you're building.

You can also just get
a `Specification<>` object easily by calling the
method `getSpecification` to pass to `JpaSpecificationExecutor` repository interfaces.

Example JSON body:

[//]: # (@formatter:off)
```json
{
  "filters": [
    {"attribute": "city", "searchOperator": "EQ", "value": "New York"},
    {"attribute": "active", "searchOperator": "EQ", "value": true}
  ],
  "conditionalOperator": "AND",
  "page": {"pageNumber": 0, "pageSize": 10},
  "order": [{"attribute": "lastName", "sortOrder": "ASC"}]
}
```
[//]: # (@formatter:on)

### Projection

The `Projection` Interface allows mapping query results directly to DTOs **without additional conversions**.

[//]: # (@formatter:off)
```java
// Also can be a POJO class instead of an Interface
public interface UserMinimalDto {
    String getFirstName();
    String getLastName();
    String getEmail();
}
```
[//]: # (@formatter:on)

Using it in a query:

```java
@Autowired
ProjectionFactory projectionFactory;

Specification<User> specification = Query.where("firstName", "John").buildSpecification();
List<UserMinimalDto> users = projectionFactory.create(User.class, UserMinimalDto.class).findAll(specification);
```

## Usage Examples

### Building Queries Directly

```java
Query<User> userQuery = Query
        .<User>where("country", SearchOperator.EQ, "USA")
        .and("age", SearchOperator.GTE, 18)
        .distinct();

Specification<User> spec = userQuery.buildSpecification();
List<User> adultsInUSA = userRepository.findAll(spec);
```

### Using SearchRequest in APIs

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

### In combination with Projection

```java

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private ProjectionFactory projectionFactory;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/search")
    public List<UserMinimalDto> getAllMinimal() {
        return projectionFactory.create(User.class, UserMinimalDto.class).findAll();
    }
}
```

## Advanced Usage

<details>
<summary>Dynamically Enhancing a <b>SearchRequest</b> Before Execution</summary>

One of the powerful features of this library is the ability to dynamically modify a `SearchRequest` at runtime before
executing it. This is particularly useful when you need to apply additional filters or conditions based on business
logic, user roles, or other dynamic factors.

Example: Enhancing a SearchRequest in a Controller

```java

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/search")
    public List<User> enhancedSearch(@RequestBody SearchRequest request) {
        Query<User> query = request.getQuery();

        query.and("status", SearchOperator.EQ, "ACTIVE"); // Global filter
        query.and("organizationId", SearchOperator.EQ, 123); // User-specific filter

        return userRepository.findAll(query.buildSpecification(), request.getPageRequest());
    }
}
```

</details>

<details>
<summary>Multiple and nested joins</summary>

```java
Query<User> query = Query.<User>where("department.manager.name", "John")
        .and("role.permission.name", "can_view_departments")
        .and(Query
                .<User>where("salary.card.bank.name", "Gotham Bank")
                .or("salary.card.bank.location", "Gotham")
                .join("salary.card.bank", JoinType.LEFT))
        .join("department.manager", JoinType.INNER)
        .join("role.permission", JoinType.INNER);

List<User> users = repository.findAll(query.buildSpecification());
```

If you omitted the `join` method,
then all the necessary tables will be joined through the given **attribute path**,
e.g. `department.manager.name` with default join type as `JoinType.LEFT_JOIN`.

```java
Query<User> query = Query.<User>where("department.manager.name", "John")
        .and("role.permission.name", "can_view_departments")
        .and(Query
                .<User>where("salary.card.bank.name", "Gotham Bank")
                .or("salary.card.bank.location", "Gotham"));

List<User> users = repository.findAll(query.buildSpecification());
```

</details>

> [!NOTE]
> Classes under `internal` package should only be used if you know and understand the
> internals. The library’s main public classes are `Query`, `SearchRequest`, `SearchOperator`, `ProjectionFactory`, and `Projection`.

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

## Package Structure

```
  dev.rosemarylab.dataquerying
  ├── api
    ├── exceptions
  │ ├── Projection.java
  │ ├── ProjectionFactory.java
  │ ├── Query.java
  │ ├── SearchOperator.java
  │ └── SearchRequest.java
  └── internal
    ├── deserializers
    ├── enums
    ├── executor
    ├── search
    ├── specification
    └── utils
```

## Contributing

Contributions are encouraged and accepted. To view more information about contribution
visit [CONTRIBUTING.md](https://github.com/aleksadacic/DataQuerying/blob/master/CONTRIBUTING.md).

## License

This project is licensed under the [Apache-2.0 license](LICENSE.txt). Feel free to modify and distribute under these
terms.
