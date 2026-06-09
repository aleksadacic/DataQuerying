# Release Notes 3.0.0

## Highlights

- Upgraded the library to the Spring Framework `7.0.x` / Spring Data JPA `4.0.x` generation.
- Raised the build and runtime baseline to Java `25`.
- Simplified the published compatibility contract to a single supported `3.0.0` baseline.

## Dependency and Tooling Updates

- Project version `2.0.1` -> `3.0.0`
- Java baseline `21` -> `25`
- Spring Data JPA `3.4.13` -> `4.0.6`
- Spring Framework BOM / Spring Test `6.2.19` -> `7.0.8`
- Jackson Databind `2.18.8` -> `2.22.0`
- JUnit Jupiter `5.14.4` -> `6.1.0`
- Hibernate Core (test scope) `6.6.53.Final` -> `7.0.10.Final`
- Lombok remains on latest stable `1.18.46`
- Mockito remains on latest stable `5.23.0`
- H2 remains on latest stable `2.4.240`
- AssertJ remains on latest stable non-milestone `3.27.7`

## Upgrade Notes

- `3.0.0` requires Java `25`.
- `3.0.0` targets Spring Framework `7`, Spring Boot `4`, and Spring Data JPA `4`.
- The library's public API was validated against the upgraded dependency stack with the existing test suite.
- Applications that need Spring `6.x` / Boot `3.x` compatibility should remain on the `2.x` release line.
