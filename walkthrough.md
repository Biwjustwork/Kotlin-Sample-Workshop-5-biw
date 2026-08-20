# Walkthrough: Simple E-commerce Inventory API (Ktor)

เราได้ทำการพัฒนาและทดสอบระบบ **Simple E-commerce Inventory API** ด้วย Kotlin และ Ktor ครบถ้วนทุกข้อกำหนดตาม [Guide.md](Guide.md) และ [implementation_plan.md](implementation_plan.md) เรียบร้อยแล้ว

---

## 1. สิ่งที่ได้สร้างและพัฒนาในโปรเจค (Summary of Changes)

### 📁 1. Domain Models & Exceptions (`src/main/kotlin/model/`)
- [`Exceptions.kt`](src/main/kotlin/model/Exceptions.kt): สร้าง Custom Domain Exceptions (`NotFoundException`, `ValidationException`, `ConflictException`, `InsufficientStockException`) และ `ErrorResponse` DTO
- [`Category.kt`](src/main/kotlin/model/Category.kt): `Category` Entity และ DTOs (`CreateCategoryRequest`, `UpdateCategoryRequest`)
- [`Product.kt`](src/main/kotlin/model/Product.kt): `Product` Entity (Many-to-One กับ Category) และ DTOs (`CreateProductRequest`, `UpdateProductRequest`, `AdjustStockRequest`)

---

### 🗄️ 2. Repository Layer (`src/main/kotlin/repository/`)
- [`CategoryRepository.kt`](src/main/kotlin/repository/CategoryRepository.kt): Data Access Interface และ `InMemoryCategoryRepository` ที่ใช้ `ConcurrentHashMap` และ `AtomicInteger`
- [`ProductRepository.kt`](src/main/kotlin/repository/ProductRepository.kt): Data Access Interface และ `InMemoryProductRepository` พร้อมเมธอด `updateStock(id, delta)` แบบ Atomic ป้องกัน Concurrency Race Condition และสต็อกติดลบ

---

### ⚙️ 3. Service Layer (`src/main/kotlin/service/`)
- [`CategoryService.kt`](src/main/kotlin/service/CategoryService.kt): Business Logic สำหรับหมวดหมู่ (ตรวจสอบชื่อไม่ว่าง, ชื่อไม่ซ้ำ, และป้องกันการลบหมวดหมู่ที่ยังมีสินค้าผูกอยู่)
- [`ProductService.kt`](src/main/kotlin/service/ProductService.kt): Business Logic สำหรับสินค้า (ตรวจสอบความถูกต้องของราคาและสต็อก >= 0, ตรวจสอบ Category ID ที่มีอยู่จริง, และกฎการเพิ่ม/ลดสต็อก)

---

### 🌐 4. Routing & Plugins (`src/main/kotlin/` & `routes/`)
- [`Serialization.kt`](src/main/kotlin/Serialization.kt): ติดตั้ง `ContentNegotiation` ร่วมกับ `kotlinx.serialization.json`
- [`StatusPages.kt`](src/main/kotlin/StatusPages.kt): ติดตั้ง Global Exception Handler แปลง Domain Exceptions เป็น HTTP Status Codes (`404`, `400`, `409`, `500`) พร้อมตอบกลับเป็น JSON `ErrorResponse`
- [`CategoryRoutes.kt`](src/main/kotlin/routes/CategoryRoutes.kt): REST API Endpoints สำหรับ `/categories` (`GET`, `POST`, `PUT`, `DELETE`)
- [`ProductRoutes.kt`](src/main/kotlin/routes/ProductRoutes.kt): REST API Endpoints สำหรับ `/products` และ `/products/{id}/add-stock`, `/products/{id}/reduce-stock`
- [`Routing.kt`](src/main/kotlin/Routing.kt): ลงทะเบียน Route และ Dependency Wiring
- [`application.yaml`](src/main/resources/application.yaml): ลงทะเบียน Module `configureSerialization`, `configureStatusPages`, และ `configureRouting`

---

### 🧪 5. Automated Tests (`src/test/kotlin/`)
- [`RepositoryTest.kt`](src/test/kotlin/repository/RepositoryTest.kt): Unit test คลังข้อมูล (3 tests)
- [`CategoryServiceTest.kt`](src/test/kotlin/service/CategoryServiceTest.kt): Unit test กฎธุรกิจ Category (7 tests)
- [`ProductServiceTest.kt`](src/test/kotlin/service/ProductServiceTest.kt): Unit test กฎธุรกิจ Product และการตัดสต็อก (7 tests)
- [`ServerTest.kt`](src/test/kotlin/ServerTest.kt): End-to-End API Integration tests ผ่าน Ktor `testApplication` (2 tests)

---

## 2. Test Execution & Verification Results

ผลการรัน Automated Test ทั้งหมดผ่าน `./gradlew.bat test`:

```
BUILD SUCCESSFUL in 2s
- RepositoryTest: 3/3 passed (100%)
- CategoryServiceTest: 7/7 passed (100%)
- ProductServiceTest: 7/7 passed (100%)
- ServerTest: 2/2 passed (100%)
Total: 19 test cases passed with 0 failures / 0 errors.
```

### รายการทดสอบหลักที่ยืนยันแล้ว:
1. ✅ **การสร้าง Category และ Product**: ตรวจสอบความสัมพันธ์ Many-to-One ได้สมบูรณ์
2. ✅ **Validation Logic**: ดักจับชื่อว่าง, ราคาติดลบ, สต็อกเริ่มต้นติดลบ ได้ถูกต้อง
3. ✅ **Stock Management**:
   - เพิ่มสต็อกสำเร็จ (`POST /products/{id}/add-stock`)
   - ตัดสต็อกสำเร็จเมื่อมีของพอ (`POST /products/{id}/reduce-stock`)
   - ปฏิเสธการตัดสต็อกเมื่อของไม่พอ พร้อมโยน `InsufficientStockException` -> HTTP 400
4. ✅ **Data Integrity**: ป้องกันการลบ Category ที่ยังมีสินค้าผูกอยู่ (`DELETE /categories/{id}` -> HTTP 409 Conflict)
5. ✅ **Global Error Handling**: StatusPages ดักจับทุก Exception และแปลงเป็น JSON มาตรฐานตามที่กำหนด

---

## 3. How to Run & Manual Test

### 1. รัน Automated Tests
```powershell
.\gradlew.bat test
```

### 2. รันเซิร์ฟเวอร์
```powershell
.\gradlew.bat run
```

### 3. ตัวอย่างการทดสอบ API ด้วย cURL

#### หมวดหมู่สินค้า (Categories):
```bash
# สร้างหมวดหมู่
curl -X POST http://localhost:8080/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Electronics"}'

# ดึงรายการหมวดหมู่ทั้งหมด
curl http://localhost:8080/categories
```

#### สินค้า (Products):
```bash
# สร้างสินค้าใหม่
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Wireless Mouse", "description": "Ergonomic 2.4GHz", "price": 499.0, "stockQuantity": 10, "categoryId": 1}'

# ดึงรายการสินค้าทั้งหมด (หรือระบุ ?categoryId=1)
curl http://localhost:8080/products?categoryId=1
```

#### การจัดการสต็อก (Stock Management):
```bash
# เติมสต็อกสินค้าเพิ่ม 5 ชิ้น
curl -X POST http://localhost:8080/products/1/add-stock \
  -H "Content-Type: application/json" \
  -d '{"amount": 5}'

# ตัดสต็อกสินค้า 3 ชิ้น
curl -X POST http://localhost:8080/products/1/reduce-stock \
  -H "Content-Type: application/json" \
  -d '{"amount": 3}'

# ตัดสต็อกเกินจำนวนคงเหลือ (ทดสอบ Validation) -> ได้ HTTP 400 Bad Request
curl -X POST http://localhost:8080/products/1/reduce-stock \
  -H "Content-Type: application/json" \
  -d '{"amount": 999}'
```
