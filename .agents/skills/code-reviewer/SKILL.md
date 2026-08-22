---
name: code-reviewer
description: >-
  Use this skill when the user asks to review code, audit code changes, inspect pull requests,
  or verify correctness in Kotlin/Ktor server applications, covering architecture layering,
  RESTful conventions, edge cases (null, negative, empty, Thai unicode), concurrency safety,
  and unit test coverage.
---

# Code Reviewer Skill for Kotlin / Ktor

This skill provides a systematic procedure and rigorous criteria for reviewing server-side Kotlin code built with Ktor. It ensures architectural integrity, data consistency, comprehensive edge case handling, and adequate unit test coverage.

---

## 🎯 Review Core Principles

When reviewing Kotlin/Ktor code, evaluate against these 5 pillars:

1. **Architecture & Layer Separation:**
   - **Routes Layer (`routes/`):** Handles HTTP request/response serialization, URL parameters, and status codes. Should contain **no business logic**.
   - **Service Layer (`service/`):** Encapsulates domain logic, validation rules, and business exceptions (e.g. `ValidationException`, `ConflictException`, `NotFoundException`).
   - **Repository Layer (`repository/`):** Manages data persistence and retrieval. Ensures thread-safety (e.g., `ConcurrentHashMap`, `AtomicInteger`) and atomic updates.

2. **RESTful API & HTTP Conventions:**
   - Proper HTTP Verbs: `GET` (retrieve), `POST` (create / non-idempotent actions), `PUT` (update/replace), `DELETE` (remove).
   - Proper HTTP Status Codes:
     - `200 OK` (successful retrieval / modification)
     - `201 Created` (resource created)
     - `204 NoContent` (resource deleted successfully)
     - `400 BadRequest` (validation failed / insufficient stock / bad payload)
     - `404 NotFound` (target entity not found)
     - `409 Conflict` (duplicate constraint / foreign key violation)
     - `500 InternalServerError` (unexpected unhandled errors)
   - Proper JSON serialization with `kotlinx.serialization` and DTO separation (`Create...Request`, `Update...Request`).

3. **Domain Validation & Edge Case Handling:**
   - **Empty / Blank Strings:** Check `.trim().isBlank()` on required text fields.
   - **Negative Values:** Check non-negative constraints for prices, quantities, and stock adjustments.
   - **Null Safety:** Leverage Kotlin's null-safety features (`?`, `?:`, `let`) and avoid `!!`.
   - **Thai & Unicode Support:** Ensure proper UTF-8 handling and trimming for international characters.

4. **Concurrency & Data Integrity:**
   - Check for race conditions in concurrent mutations.
   - Use atomic operations (e.g. `computeIfPresent`) or database transactions.
   - Prevent inventory/stock from dropping below zero under concurrent access.

5. **Unit Test Quality & Coverage:**
   - Business Logic & Service layer must have unit tests covering both happy paths and failure/exception cases.
   - Repository layer must test data isolation and atomic updates.
   - Tests must assert expected exceptions using `assertFailsWith<ExpectedException> { ... }`.

---

## 📋 Step-by-Step Review Workflow

### Step 1: Inspect Changes & Affected Files
- If reviewing git changes, check modified files using git diff or view file contents.
- Identify newly added or modified Routes, Services, Repositories, Models, and Tests.

### Step 2: Systematic Checklist Verification
Walk through the checklist for each layer:
- [ ] Are models properly annotated with `@Serializable`?
- [ ] Is input validation placed in the Service layer?
- [ ] Are custom exceptions mapped to appropriate HTTP status codes in `StatusPages`?
- [ ] Are foreign key / relational constraints verified before delete/create?
- [ ] Are stock / counter operations atomic and thread-safe?
- [ ] Are unit tests written to assert all new business rules and edge cases?

### Step 3: Run Automated Test Suite
- Run the test suite to verify that existing and new tests pass:
  ```powershell
  ./gradlew test
  ```
- If tests fail, investigate the root cause and document the failure.

### Step 4: Generate Structured Review Report
Format the review findings using the severity classification below:

- 🚨 **[Critical]**: Security flaws, data corruption, uncaught runtime exceptions, broken business logic, or missing fundamental tests.
- ⚠️ **[Warning]**: Violation of layering boundaries, missing edge case validation (e.g. negative numbers, blank strings), or sub-optimal status codes.
- 💡 **[Suggestion]**: Idiomatic Kotlin improvements, cleaner DTO modeling, code readability, or performance optimizations.
- ✅ **[Good Practice]**: Commendable patterns, excellent test coverage, and clean concurrency handling.

---

## 📝 Review Report Template

Provide the review response in Thai (with English code and identifiers) using this structure:

```markdown
### 📊 สรุปภาพรวมการ Review (Review Summary)
- **ไฟล์ที่ตรวจสอบ:** ...
- **ผลการทดสอบ (Unit Tests):** ผ่านทั้งหมด / มีข้อผิดพลาด
- **ระดับความพร้อม (Readiness):** พร้อมใช้งาน / ต้องแก้ไขก่อนรวมโค้ด

---

### 🔍 รายละเอียดผลการตรวจสอบ (Detailed Findings)

#### 🚨 ข้อควรแก้ไขเร่งด่วน (Critical)
- **ปัญหา:** [ระบุตำแหน่งและปัญหา]
- **ผลกระทบ:** [อธิบายผลเสียที่อาจเกิดขึ้น]
- **วิธีแก้ไข (Before / After Code):**
  ```kotlin
  // Before
  ...
  // After
  ...
  ```

#### ⚠️ ข้อควรระวังและปรับปรุง (Warnings)
- [ประเด็น เช่น Edge Case, Status Code ที่ไม่เหมาะสม]

#### 💡 ข้อเสนอแนะเพื่อความคลีน (Suggestions)
- [ประเด็นการใช้ Kotlin Idioms หรือการปรับโครงสร้าง]

#### ✅ จุดเด่นที่ทำได้ดี (Good Practices)
- [ชื่นชมแนวทางที่ดี เช่น Layering ชัดเจน, Test ครอบคลุม]
```
