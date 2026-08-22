---
name: kotlin-developer
description: >-
  Use this skill when implementing new features, endpoints, domain models, services,
  repositories, or unit tests in Kotlin/Ktor server projects, following clean 3-tier
  layering architecture, thread-safety, and edge-case handling.
---

# Kotlin / Ktor Backend Developer Skill

This skill guides the implementation of server-side features in Kotlin using Ktor, ensuring clean architecture, type safety, robust domain validation, and comprehensive unit tests.

---

## 🏗️ 5-Layer Development Blueprint

When developing any feature or endpoint, follow this layer-by-layer implementation sequence:

```
1. Model & DTOs (Serializable data contracts)
   └── 2. Repository Layer (Data access interface & thread-safe storage)
       └── 3. Service Layer (Domain logic, validation, custom exceptions)
           └── 4. Routing Layer (HTTP endpoints & status codes)
               └── 5. Unit Tests (Service & Repository test cases)
```

---

## 🛠️ Implementation Steps

### Step 1: Define Domain Models & DTOs (`model/`)
- Annotate data classes and DTOs with `@Serializable` from `kotlinx.serialization`.
- Separate persistent domain models from request DTOs (`Create<Entity>Request`, `Update<Entity>Request`).
- Keep immutable properties (`val`) wherever possible.

```kotlin
@Serializable
data class Item(
    val id: Int,
    val name: String,
    val price: Double
)

@Serializable
data class CreateItemRequest(
    val name: String,
    val price: Double
)
```

### Step 2: Define & Implement Repository (`repository/`)
- Create an `interface` defining all data access operations.
- For in-memory implementations, ensure **thread-safety** using `ConcurrentHashMap` and `AtomicInteger`.
- For mutable fields (like stock or counters), use atomic update methods (`computeIfPresent`).

```kotlin
interface ItemRepository {
    fun getAll(): List<Item>
    fun getById(id: Int): Item?
    fun create(name: String, price: Double): Item
    fun update(id: Int, name: String, price: Double): Item?
    fun delete(id: Int): Boolean
}
```

### Step 3: Implement Business Logic & Validation (`service/`)
- Encapsulate all business validation rules and foreign key integrity checks in the Service layer.
- Never write business logic inside routes.
- Throw appropriate domain exceptions:
  - `NotFoundException`: Entity does not exist.
  - `ValidationException`: Input invalid (blank name, negative value).
  - `ConflictException`: Duplicate unique key or relational dependency conflict.
- **Mandatory Edge Case Checks:**
  - `name.trim().isBlank()` -> throw `ValidationException("... cannot be blank")`
  - `price < 0.0` or `amount <= 0` -> throw `ValidationException("... must be non-negative")`
  - Null safety handling via `?`, `?:`, and safe calls.
  - Full support for Thai / Unicode strings in input fields.

### Step 4: Wire HTTP Routing (`routes/` & `Routing.kt`)
- Map endpoints using standard RESTful HTTP Verbs and Status Codes:
  - `GET /items` -> `HttpStatusCode.OK`
  - `GET /items/{id}` -> `HttpStatusCode.OK`
  - `POST /items` -> `HttpStatusCode.Created`
  - `PUT /items/{id}` -> `HttpStatusCode.OK`
  - `DELETE /items/{id}` -> `HttpStatusCode.NoContent`
- Parse parameters safely:
  ```kotlin
  val id = call.parameters["id"]?.toIntOrNull()
      ?: throw ValidationException("Invalid ID format. Must be an integer.")
  ```

### Step 5: Write Unit Tests (`src/test/kotlin/`)
- Always write unit tests alongside newly implemented code.
- Write tests for both happy paths and error scenarios:
  - Test validation failures (`assertFailsWith<ValidationException>`).
  - Test not found scenarios (`assertFailsWith<NotFoundException>`).
  - Test duplicate / conflict constraints (`assertFailsWith<ConflictException>`).
  - Test Thai unicode strings and special characters.
