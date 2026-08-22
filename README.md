# Simple E-commerce Inventory API (Ktor)

ระบบ RESTful API สำหรับบริหารจัดการคลังสินค้าของร้านค้าออนไลน์ขนาดเล็ก พัฒนาด้วย **Kotlin** และ **Ktor Server** ออกแบบตามสถาปัตยกรรม **3-Tier Layered Architecture** (Routing → Service → Repository → Model) พร้อมระบบจัดการความสัมพันธ์แบบ Many-to-One, การควบคุม Concurrency ป้องกันสต็อกสินค้าติดลบ, และระบบค้นหาสินค้าตามช่วงราคา (Price Filter)

---

## 🛠️ เทคโนโลยีที่ใช้ (Tech Stack)

* **Language:** Kotlin 2.4.0 (JVM Toolchain 21)
* **Framework:** Ktor 3.5.0 (EngineMain, Netty)
* **Serialization:** `kotlinx.serialization` (JSON Content Negotiation)
* **Error Handling:** Ktor `StatusPages` Plugin
* **Logging:** Logback Classic 1.5.35
* **Testing:** Kotlin Test & Ktor Server Test Host (`testApplication`)

---

## 🏛️ สถาปัตยกรรมระบบ (System Architecture)

ระบบแบ่งแยกหน้าที่ตามหลักการ **Separation of Concerns** ออกเป็น 4 เลเยอร์หลัก:

```mermaid
flowchart TD
    Client(["HTTP Client / Postman / cURL"])
    
    subgraph Ktor Application
        StatusPages["🛡️ StatusPages Plugin (Global Error Handler)"]
        ContentNeg["🔄 ContentNegotiation (JSON Serializer)"]
        
        subgraph Routing Layer
            CatRoute["📁 CategoryRoutes (/categories)"]
            ProdRoute["📦 ProductRoutes (/products)"]
        end
        
        subgraph Service Layer (Business Logic)
            CatService["⚙️ CategoryService"]
            ProdService["⚙️ ProductService"]
        end
        
        subgraph Repository Layer (Data Access & Concurrency)
            CatRepo["🗄️ InMemoryCategoryRepository"]
            ProdRepo["🗄️ InMemoryProductRepository (Atomic Updates)"]
        end
        
        subgraph Domain Models
            Models["📋 Entities, DTOs & Custom Exceptions"]
        end
    end

    Client <-->|HTTP JSON Request / Response| ContentNeg
    ContentNeg <--> Routing Layer
    Routing Layer <--> Service Layer
    Service Layer <--> Repository Layer
    Repository Layer <--> Models
    Routing Layer -.->|Throw ValidationException| StatusPages
    Service Layer -.->|Throw Domain Exceptions| StatusPages
    StatusPages -->|HTTP Error Status + JSON ErrorResponse| Client
```

### หน้าที่ของแต่ละ Layer:
1. **Routing Layer (`routes/`):** รับคำขอ HTTP, แปลง Path/Query Parameters อย่างปลอดภัย (`toIntOrNull()`, `toDoubleOrNull()`), ส่งต่อให้ Service และส่งคืน HTTP Status Code ที่ถูกต้อง (ไม่มี Business Logic ปะปน)
2. **Service Layer (`service/`):** ศูนย์กลางตรรกะทางธุรกิจ (Business Rules) เช่น การตรวจความถูกต้องของราคาและสต็อก, การตรวจสอบความมีอยู่จริงของ Category, และการคำนวณตัดสต็อก
3. **Repository Layer (`repository/`):** จัดการการเข้าถึงและแก้ไขข้อมูลในหน่วยความจำแบบ **Thread-Safe** โดยใช้ `ConcurrentHashMap` และ Atomic Operations (`computeIfPresent`) เพื่อป้องกัน Race Condition
4. **Model Layer (`model/`):** โมเดลข้อมูล Entity, Request/Response DTOs เพื่อป้องกันปัญหา Mass Assignment และ Custom Domain Exceptions

---

## 📊 โมเดลข้อมูล (Data Models & DTOs)

### 1. Entity Models (ความสัมพันธ์ Many-to-One)
* **Category:** `id: Int`, `name: String`
* **Product:** `id: Int`, `name: String`, `description: String?`, `price: Double`, `stockQuantity: Int`, `categoryId: Int` *(สินค้าแต่ละชิ้นสังกัด 1 หมวดหมู่)*

### 2. Request DTOs
* `CreateCategoryRequest(name: String)`
* `UpdateCategoryRequest(name: String)`
* `CreateProductRequest(name: String, description: String?, price: Double, stockQuantity: Int, categoryId: Int)`
* `UpdateProductRequest(name: String, description: String?, price: Double, categoryId: Int)`
* `AdjustStockRequest(amount: Int)`

---

## 📡 รายการ API Endpoints (RESTful API Specification)

### 1. หมวดหมู่สินค้า (Categories)

| Method | Endpoint | Description | Success Status | Error Statuses |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/categories` | ดึงรายชื่อหมวดหมู่ทั้งหมด | `200 OK` | - |
| `GET` | `/categories/{id}` | ดึงข้อมูลหมวดหมู่ตาม ID | `200 OK` | `404 Not Found`, `400 Bad Request` |
| `POST` | `/categories` | สร้างหมวดหมู่ใหม่ | `201 Created` | `400 Bad Request`, `409 Conflict` |
| `PUT` | `/categories/{id}` | แก้ไขชื่อหมวดหมู่ | `200 OK` | `404 Not Found`, `400 Bad Request`, `409 Conflict` |
| `DELETE` | `/categories/{id}` | ลบหมวดหมู่ *(ไม่อนุญาตถ้ามีสินค้าผูกอยู่)* | `204 No Content` | `404 Not Found`, `409 Conflict` |

---

### 2. รายการสินค้าและการค้นหา (Products & Price Filter)

| Method | Endpoint | Query Parameters | Description | Success Status | Error Statuses |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/products` | `categoryId`, `minPrice`, `maxPrice` | ค้นหาและกรองรายการสินค้า | `200 OK` | `400 Bad Request`, `404 Not Found` |
| `GET` | `/products/{id}` | - | ดึงข้อมูลสินค้าตาม ID | `200 OK` | `404 Not Found`, `400 Bad Request` |
| `POST` | `/products` | - | สร้างสินค้าใหม่ | `201 Created` | `400 Bad Request`, `404 Not Found` |
| `PUT` | `/products/{id}` | - | แก้ไขรายละเอียดสินค้า | `200 OK` | `404 Not Found`, `400 Bad Request` |
| `DELETE` | `/products/{id}` | - | ลบสินค้าตาม ID | `204 No Content` | `404 Not Found`, `400 Bad Request` |

---

### 3. การจัดการสต็อกสินค้า (Stock Management)

| Method | Endpoint | Payload | Description | Success Status | Error Statuses |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/products/{id}/add-stock` | `{"amount": Int}` | เติมจำนวนสต็อกสินค้า | `200 OK` | `400 Bad Request`, `404 Not Found` |
| `POST` | `/products/{id}/reduce-stock`| `{"amount": Int}` | ลดสต็อกสินค้า *(ป้องกันสต็อกติดลบ)* | `200 OK` | `400 Bad Request`, `404 Not Found` |

---

## 🔒 กฎทางธุรกิจและการจัดการความปลอดภัย (Business Rules & Integrity)

1. **การป้องกันสต็อกติดลบ (Zero/Negative Stock Protection):**
   * การตัดสต็อก (`reduce-stock`) ดำเนินการผ่าน Atomic block `computeIfPresent` ใน `ProductRepository` หากจำนวนที่ตัดมากกว่าสต็อกคงเหลือ ระบบจะยกเลิกการเปลี่ยนแปลงและโยน `InsufficientStockException` ทันที
2. **การรักษาความสมบูรณ์ของความสัมพันธ์ (Referential Integrity):**
   * ไม่อนุญาตให้สร้าง/แก้ไข Product โดยระบุ `categoryId` ที่ไม่มีอยู่ในระบบ
   * ไม่อนุญาตให้ลบ Category ที่ยังมีสินค้าผูกอยู่ โดยระบบจะแจ้งเตือนเป็น HTTP `409 Conflict`
3. **การตรวจสอบความถูกต้องของข้อมูล (Validation):**
   * ชื่อสินค้าและหมวดหมู่ต้องไม่ว่างเปล่า (`not blank`)
   * ราคา (`price`), สต็อกเริ่มต้น (`stockQuantity`), และยอดปรับปรุงสต็อก (`amount`) ต้องไม่ติดลบ
   * ตัวกรองราคา: `minPrice >= 0.0`, `maxPrice >= 0.0` และ `minPrice <= maxPrice`
4. **การรวมศูนย์จัดการข้อผิดพลาด (Centralized Error Handling):**
   * ใช้ Ktor `StatusPages` แปลง Domain Exception ทุกตัวเป็น JSON `ErrorResponse` ที่มีรูปแบบเดียวกัน:
     ```json
     {
       "status": 400,
       "message": "minPrice (500.0) cannot be greater than maxPrice (100.0)",
       "timestamp": 1771500000000
     }
     ```

---

## 🚀 การติดตั้งและเปิดใช้งาน (Getting Started)

### ข้อกำหนดเบื้องต้น
* JDK 21 หรือสูงกว่า

### 1. รันชุดทดสอบ Automated Tests
```powershell
# Windows
.\gradlew.bat test

# macOS / Linux
./gradlew test
```

### 2. สั่งรันเซิร์ฟเวอร์ (Start Server)
```powershell
# Windows
.\gradlew.bat run

# macOS / Linux
./gradlew run
```
*เซิร์ฟเวอร์จะเริ่มต้นทำงานที่พอร์ต `http://localhost:8080`*

---

## 🧪 ตัวอย่างคำสั่งทดสอบ API (cURL Examples)

### 1. กรณีใช้งานปกติ (Happy Path)

#### 1.1 สร้างหมวดหมู่สินค้า
```bash
curl -X POST http://localhost:8080/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Gaming Gears"}'
```

#### 1.2 สร้างสินค้าใหม่
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "description": "RGB Hot-swappable",
    "price": 2490.0,
    "stockQuantity": 15,
    "categoryId": 1
  }'
```

#### 1.3 ค้นหาสินค้าตามช่วงราคา (Price Filter)
```bash
# ค้นหาสินค้าราคา 1,000 ถึง 3,000 บาท ในหมวดหมู่ที่ 1
curl "http://localhost:8080/products?categoryId=1&minPrice=1000&maxPrice=3000"
```

#### 1.4 เติมสต็อกสินค้า (+10 ชิ้น)
```bash
curl -X POST http://localhost:8080/products/1/add-stock \
  -H "Content-Type: application/json" \
  -d '{"amount": 10}'
```

#### 1.5 ตัดสต็อกสินค้า (-5 ชิ้น)
```bash
curl -X POST http://localhost:8080/products/1/reduce-stock \
  -H "Content-Type: application/json" \
  -d '{"amount": 5}'
```

---

### 2. กรณีตรวจสอบข้อผิดพลาดและ Edge Cases (Error Cases)

#### 2.1 ตัดสต็อกเกินจำนวนคงเหลือ (HTTP 400 Bad Request)
```bash
curl -X POST http://localhost:8080/products/1/reduce-stock \
  -H "Content-Type: application/json" \
  -d '{"amount": 999}'
```

#### 2.2 ค้นหาด้วยช่วงราคาที่ไม่ถูกต้อง `minPrice > maxPrice` (HTTP 400 Bad Request)
```bash
curl "http://localhost:8080/products?minPrice=5000&maxPrice=1000"
```

#### 2.3 สร้างสินค้าด้วย Category ID ที่ไม่มีอยู่จริง (HTTP 404 Not Found)
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mousepad",
    "price": 350.0,
    "stockQuantity": 20,
    "categoryId": 999
  }'
```

#### 2.4 ลบ Category ที่ยังมีสินค้าสังกัดอยู่ (HTTP 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/categories/1
```
