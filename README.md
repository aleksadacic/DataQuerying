# Spring Data Querying

A *lightweight library*
for building dynamic, type-safe queries in Spring Data JPA.  
This library provides a convenient DSL to construct JPA queries at runtime using objects like `Query`, `SearchRequest`,
and `SearchOperator`.

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
- **Nested Filters**: Build recursive AND/OR conditions through `SearchRequest`.
- **Minimal Boilerplate**: No need to hand-write every `specification<T>` --the library does it for you.
- **Integration with Spring Data**: Seamlessly use `specification<T>`, `Pageable`, etc.

## Installation

<details>
<summary>Gradle</summary>

```groovy
dependencies {
    implementation 'com.aleksadacic.springdataquerying:spring-data-querying:YOUR_VERSION'
}
```

</details>

<details>
<summary>Maven</summary>

```xml

<dependency>
    <groupId>com.aleksadacic.springdataquerying</groupId>
    <artifactId>spring-data-querying</artifactId>
    <version>YOUR_VERSION</version>
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

```ajava
public enum SearchOperator {
    EQ,
    NOT_EQ,
    GTE,
    LTE,
    GT,
    LT
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

```ajava
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

```ajava
  Query<User> userQuery = Query.get()
  .where("country", SearchOperator.EQ, "USA")
  .and("age", SearchOperator.GTE, 18)
  .distinct();
  
  Specification<User> spec = userQuery.buildSpecification();
  List<User> adultsInUSA = userRepository.findAll(spec);
```

### Using SearchRequest in APIs

Create a `SearchRequest<User>` on the client side (or in your code) that includes filters and pagination:

```ajson
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

```ajava
    @RestController
    @RequestMapping("/users")
    public class UserController {

        @Autowired
        private UserRepository userRepository;

        @PostMapping("/search")
        public List<User> searchUsers(@RequestBody SearchRequest<User> request) {
            Query<User> query = SearchRequestQueryTransformer.toQuery(request);
            Specification<User> spec = query.buildSpecification();
            return userRepository.findAll(spec, request.getPageRequest()).getContent();
        }
    }
```

Execute the query with your repository, taking advantage of the PageRequest or Sort from the SearchRequest.

## Advanced Usage

- Nested Filters with AND/OR groups:
  `FilterData` supports a list of child filters, which themselves can have nested filters. The library recursively
  builds
  a
  combined `Specification` or `CriteriaQuery`.

- Joins: The `Query` object supports `.join("attribute.subAttribute", JoinType.LEFT)`.
- DTO Projection: Using `executeQuery(...)` allows you to project selected fields into a custom DTO class.

> [!NOTE]
> Most of these classes & operations are under internal packages, so you should only rely on them if you know the
> internals. The library’s main public classes are `Query`, `SearchRequest`, and `SearchOperator`.

## Exceptions

When using the dynamic query features in this library, you may encounter the following exceptions. They are typically
thrown if your filter definitions or query parameters are invalid. Catch them where appropriate, or surface them as HTTP
errors in your REST APIs.

- `AttributeNotFoundException` (`SpecificationBuilderException`)  
  Thrown when the specified attribute does not exist on the entity when trying to create `Specification` object. For instance, if your query references a field that
  isn’t a valid column/property, this exception indicates the attribute cannot be resolved.


- `JoinNotFoundException` (`SpecificationBuilderException`)  
  Thrown when attempting to perform a `join` on a relationship that doesn't exist or isn’t properly mapped. This might
  happen if you reference a nested path (e.g., `user.address.city`) and one of the segments isn’t a valid association.


- `MappingException` (`RuntimeException`)  
  Signals a more general mapping error. For example, if the library is unable to convert an input value into the correct
  type needed for the query or if there’s a mismatch between the filter type and the entity’s field type.


- `SpecificationBuilderException` (`RuntimeException`)  
  This is a wrapper for the `RuntimeException`. Thrown if there is an error in building the `Specification`—for example,
  conflicting operators, logical errors in
  filter groupings, or issues that prevent the library from creating a valid JPA criteria.

In most cases, these exceptions indicate an issue with how your filters or joins are configured. Proper validation of
incoming request data (e.g., attribute names, operator types) can help avoid them.

## Package Structure

```
  com.aleksadacic.springdataquerying
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

- Internal packages contain supporting classes (e.g., `FilterData`, `SpecificationEngine`) not intended for direct use.
- Only api classes are part of the stable, public-facing API.

## Contributing

1. Fork the repository and create your feature branch `git checkout -b feature/new-cool-feature`.
2. Commit your changes `git commit -m 'Add some new feature'`.
3. Push to the branch `git push origin feature/new-cool-feature`.
4. Create a Pull Request.

Issues and bug reports are welcome—please file them via the GitHub Issues section.

## License

This project is licensed under the [MIT License](LICENSE.txt). Feel free to modify and distribute under these terms.