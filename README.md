# 🛒 E-Commerce Application (Microservices)

A modular, microservice-based e-commerce application built using **Spring Boot**, **JPA/Hibernate**, **DTO-based validation**, and **auditing**.

---

## Table of Contents

1. [Overview](#overview)
2. [Technologies](#technologies)
3. [Microservices](#microservices)
4. [Getting Started](#getting-started)
5. [Architecture & Project Structure](#architecture--project-structure)
6. [Entity Design](#entity-design)
7. [DTO & Validation](#dto--validation)
8. [Exception Handling](#exception-handling)
9. [Auditing](#auditing)
10. [API Endpoints](#api-endpoints)
11. [License](#license)

---

## overview

This e-commerce application demonstrates:

- Clean microservices architecture
- Separation of **DTOs** and **Entities**
- **Audited entities** with `createdAt` and `updatedAt`
- Centralized **ErrorCode + BusinessException** pattern
- Stable `equals`/`hashCode` for entities using UUIDs
- Safe `toString()` for logging, ignoring lazy-loaded children

---

## Technologies

- Java 21
- Spring Boot 3.x
- Spring Data JPA / Hibernate
- Jakarta Bean Validation
- Lombok
- MapStruct (DTO <-> Entity mapping)
- PostgreSQL / H2 (for dev/testing)
- Maven

---

## Microservices

| Service                | Port | Description |
|------------------------|------|-------------|
| **product-service**    | 8083 | Manage products and categories |
| **user-service**       | 8081 | Manage user accounts and roles |
| **order-service**      | 8084 | Handle orders, carts, and checkout (optional/future) |
| **auth-service**       | 8082 | Authentication & JWT token management |
| **inventory-service**  | 8085 | Inventory for stock of the products|
|**gatway-service**| 8888 | gate way for all microservices|


---

## Getting Started

1. Clone the repository:

```
git clone <repo-url>
cd ecommerce-application

Run services individually:
./mvnw spring-boot:run -pl product-service
./mvnw spring-boot:run -pl user-service
./mvnw spring-boot:run -pl order-service
./mvnw spring-boot:run -pl auth-service
Access APIs (Swagger/OpenAPI):
http://localhost:<service-port>/swagger-ui.html
```

## Architecture & Project Structure

Each microservice follows the same structure:
````
service-name/
├── entity/          # JPA entities
├── dto/             # Request/Response DTOs
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic
├── exception/       # BusinessException + ErrorCode
├── audit/           # BaseEntity for createdAt / updatedAt
├── rest/            # REST controllers
├── filter/          # filter for authentication
├── advice/          # for global exceptional handler
├── handler/         # gloabal exceptional handler for security
├── grpc/            # grpc package 
├── mapper/          # mapper class
├── validation/      # validator
├── config/          # configuration classes

````

## Entity Design
````
ProductEntity Example
@Entity
@Table(name = "products")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    @ToString.Include
    private String sku;

    // other fields...
}
````

## DTO & Validation
````
Jakarta Bean Validation annotations are applied only on DTOs:
public record CreateUserRequestDto(
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @Email @NotBlank String email
) {}



 Spring validates automatically using @Valid in controllers.
````
## Exception Handling
```
BusinessException carries an ErrorCode and optional message.
ErrorCode maps to HTTP status and a unique code:

public enum ErrorCode {
    USER_NOT_FOUND( HttpStatus.NOT_FOUND, "User not found"),
    PRODUCT_NOT_FOUND( HttpStatus.NOT_FOUND, "Product not found"),
    CATEGORY_NOT_FOUND( HttpStatus.NOT_FOUND, "Category not found")
}

A global exception handler translates exceptions into meaningful API responses.



````
## Auditing
````
createdAt and updatedAt are automatically maintained by Spring Data JPA auditing.
Included in entity toString() for debugging.

BaseEntity (Auditing)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public abstract class BaseEntity {
    @CreatedDate
    @ToString.Include
    private Instant createdAt;

    @LastModifiedDate
    @ToString.Include
    private Instant updatedAt;
}
````

## API Endpoints (High-Level)
| Service          | Endpoint        | Method | Description     |
|------------------|-----------------|--------|-----------------|
| Product Service  | /products       | POST   | Create product  |
| Product Service  | /products/{id}  | GET    | Fetch product   |
| Category Service | /categories     | POST   | Create category |
| User Service     | /users          | POST   | Create user     |
| User Service     | /users/{id}     | GET    | Fetch user      |
Each service can be tested independently or integrated via an API Gateway in the future.

## License

MIT License © 2026


---

This single README combines:

- Microservice overview  
- Project structure  
- Entity & DTO design  
- Auditing  
- Exception handling  
- API endpoints  
- Usage instructions  

---

