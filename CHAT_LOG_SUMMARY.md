# บันทึกสรุปการเรียนรู้และการสนทนา (AI Log Summary)

บันทึกสรุปการใช้งาน AI เพื่อประกอบการส่งการบ้าน (AI Log) สำหรับการพัฒนาระบบ RESTful API จัดการคลังสินค้า (Simple E-commerce Inventory API) ด้วย Kotlin และ Ktor Server พร้อมการทดสอบด้วย Unit Test และ Integration Test ภายใต้ข้อกำหนดการเรียนรู้ของ Workshop 5: Kotlin for Server-Side Development

---

## 1. Prompt ที่ใช้ (สรุป)
* **การวิเคราะห์ข้อกำหนดจาก Guide.md และออกแบบสถาปัตยกรรม (Planning Mode):**
  * สั่งการด้วยคำสั่ง `/plan` ร่วมกับไฟล์ [Guide.md](file:///D:/Project/ktor-workshop-5/Guide.md) เพื่อให้ AI อ่านข้อกำหนด, วิเคราะห์ระบบ และออกแบบสถาปัตยกรรม Layered Architecture (Routing -> Service -> Repository -> Model) พร้อมอธิบายเหตุผลและหลักการออกแบบให้เข้าใจอย่างแท้จริง
* **การขอสร้างไฟล์แผนงานลงในโปรเจกต์:**
  * สั่งให้ AI สร้างไฟล์ [implementation_plan.md](file:///D:/Project/ktor-workshop-5/implementation_plan.md) ลงในโฟลเดอร์ Root ของโปรเจกต์โดยตรงเพื่อให้อ่านและตรวจสอบความพร้อมก่อนอนุมัติ
* **การสอบถามแนวคิดเรื่อง Custom Domain Exceptions และ StatusPages:**
  * สอบถาม AI ให้อธิบายความหมายและประโยชน์ของ *Custom Domain Exceptions* และ *Global Exception Handling with StatusPages* สำหรับผู้เริ่มต้น เพื่อทำความเข้าใจว่าทำไมจึงต้องใช้และทำงานร่วมกันอย่างไร
* **การอนุมัติและดำเนินการพัฒนาโปรเจกต์ (Implementation):**
  * อนุมัติให้ AI ดำเนินการสร้างและแก้ไขโค้ดตามแผนที่วางไว้ (เพิ่ม Dependencies, สร้าง Data Models/DTOs, Repositories, Services, Routes, StatusPages, ContentNegotiation และชุด Automated Tests)
* **การสร้าง Walkthrough สรุปผลงาน:**
  * สั่งให้ AI บันทึกไฟล์ [walkthrough.md](file:///D:/Project/ktor-workshop-5/walkthrough.md) ลงในโปรเจกต์เพื่อสรุปรายการไฟล์ที่สร้าง, ผลการรัน Test และตัวอย่างคำสั่ง cURL สำหรับทดสอบ API
* **การสั่งเพิ่มฟีเจอร์ Price Filter ตามข้อกำหนด AGENTS.md:**
  * สั่งให้ AI เพิ่มระบบค้นหาสินค้าตามช่วงราคา (`minPrice`, `maxPrice`) บน Endpoint `GET /products` พร้อมย้ำเตือนให้อ่านและปฏิบัติตามกฎใน [AGENTS.md](file:///D:/Project/ktor-workshop-5/AGENTS.md) (Multi-Agent Orchestration, ทดสอบคู่กับโค้ดเสมอ, จัดการ Edge Cases ครอบคลุม Empty List, ค่าติดลบ, Null, ภาษาไทย)
* **การอัปเดตบันทึกการสนทนาล่าสุด:**
  * สั่งให้ AI อัปเดตไฟล์ `CHAT_LOG_SUMMARY.md` เพื่อบันทึกรายละเอียดการสนทนาและการพัฒนาฟีเจอร์ค้นหาตามช่วงราคาล่าสุดลงไปอย่างครบถ้วน

---

## 2. AI ตอบผิด / น่าสงสัยตรงไหน
* **ปัญหา Overload Resolution Ambiguity ใน StatusPages.kt:**
  * ในขั้นตอนแรกที่มีการคอมไพล์โค้ด เกิด Error `Overload resolution ambiguity between candidates: class NotFoundException : DomainException vs class NotFoundException : Exception` เนื่องจากมีการใช้ Wildcard Import (`import io.ktor.server.plugins.*` ชนกับ `import com.example.model.*`) ทำให้ Ktor สับสนระหว่าง Exception ของระบบและ Custom Domain Exception ที่สร้างขึ้นเอง
  * **การแก้ไข:** ปรับเปลี่ยนการ Import ให้เป็นแบบ Explicit Class Names และเขียน Handler ดักจับแยกคลาสกันอย่างชัดเจน ส่งผลให้คอมไพล์ผ่านและทำงานได้อย่างสมบูรณ์
* **การจัดการ Dependency Version Catalog:**
  * โปรเจกต์ใช้ Gradle Version Catalog (`libs.versions.toml` และ `ktorLibs`) แต่ยังขาดการผูก Plugin `kotlin.plugin.serialization` และ `ktor-server-status-pages` ทาง AI จึงต้องเพิ่มการประกาศ plugin ใน `libs.versions.toml` และใส่ dependencies ใน `build.gradle.kts` ให้ครบถ้วน
* **การจัดการ Type Parsing ของ Query Parameters ป้องกัน Uncaught Exceptions:**
  * หาก parse query string ด้วย `.toDouble()` โดยตรง เมื่อผู้ใช้ส่งค่าที่ไม่ใช่ตัวเลข (เช่น `?minPrice=abc`) โปรแกรมจะ throw `NumberFormatException` ที่ไม่ได้ตั้งใจ
  * **การแก้ไข:** ใช้ `.toDoubleOrNull() ?: throw ValidationException(...)` เพื่อแปลงเป็น Custom Domain Exception ที่ควบคุมได้และส่งผลลัพธ์ HTTP 400 Bad Request สวยงามกลับไป

---

## 3. เราตัดสินใจ / แก้อย่างไร
* **การใช้ Layered Architecture (Separation of Concerns):**
  * แบ่งโค้ดออกเป็น 4 Layer ชัดเจน:
    1. **Model Layer:** Entity Data Classes, Request/Response DTOs และ Domain Exceptions
    2. **Repository Layer:** Interface และ In-Memory Data Access จัดเก็บข้อมูลด้วย `ConcurrentHashMap` และ `AtomicInteger`
    3. **Service Layer:** ศูนย์รวม Business Logic (ตรวจสอบความถูกต้องของข้อมูล, ตรวจสอบ Category ID ที่มีอยู่จริง, กฎการเพิ่ม/ลดสต็อก)
    4. **Routing Layer:** รับ HTTP Request, ตรวจสอบ Parameter เบื้องต้น และส่งผลลัพธ์พร้อม HTTP Status Code
* **การแยก Entity และ DTO (Data Transfer Object):**
  * สร้าง `CreateProductRequest`, `UpdateProductRequest`, `CreateCategoryRequest`, `AdjustStockRequest` แยกจาก Entity หลัก เพื่อป้องกันปัญหา Over-posting / Mass Assignment
* **การรักษา Data Integrity และ Concurrency Safety ในการตัดสต็อก:**
  * ออกแบบเมธอด `updateStock(id, delta)` ใน `ProductRepository` ให้ทำการคำนวณแบบ Atomic Operation หากสต็อกติดลบ (`newStock < 0`) จะไม่ปรับปรุงข้อมูล และ throw `InsufficientStockException` ทันที ป้องกันปัญหา Race Condition เมื่อมีคำสั่งซื้อเข้ามาพร้อมกัน
* **การควบคุมความสัมพันธ์แบบ Many-to-One และ Referential Integrity:**
  * ก่อนสร้าง/แก้ไข Product ต้องตรวจสอบว่า Category ID มีอยู่จริง
  * ป้องกันการลบ Category หากยังมี Product สังกัดอยู่ โดย throw `ConflictException` (ส่งกลับ HTTP 409 Conflict)
* **การพัฒนาระบบค้นหาสินค้าตามช่วงราคา (Price Filter: `minPrice`, `maxPrice`):**
  * **Repository:** ปรับปรุง `ProductRepository.getAll(categoryId, minPrice, maxPrice)` ใช้ Kotlin Sequence `asSequence().filter {...}.sortedBy {...}.toList()` เพื่อกรองข้อมูลอย่างมีประสิทธิภาพ
  * **Service:** เพิ่มกฎ Business Validation ใน `ProductService.getAllProducts`:
    - `minPrice >= 0.0` และ `maxPrice >= 0.0`
    - `minPrice <= maxPrice` (หาก `minPrice > maxPrice` จะ throw `ValidationException`)
    - ตรวจสอบ `categoryRepository.existsById(categoryId)` หากระบุหมวดหมู่
  * **Routing:** อัปเดต `ProductRoutes.kt` ให้ parse query parameters `minPrice` และ `maxPrice` อย่างปลอดภัย
* **การใช้ Multi-Agent Orchestration Protocol ตามข้อกำหนด AGENTS.md:**
  * ใช้ **Developer Subagent** พัฒนาโค้ดและ Unit Tests
  * ใช้ **Reviewer Subagent** ทำการ Audit คุณภาพโค้ดตามเกณฑ์ `code-reviewer` และรัน `./gradlew test`
  * ผลการประเมินจาก Reviewer: **Production Ready (0 Critical / 0 Warning)**
* **การทดสอบอย่างครอบคลุมด้วย Automated Tests (ครอบคลุม Edge Cases ทั้งหมด):**
  * ครอบคลุม: กรองเดี่ยว (`minPrice` หรือ `maxPrice`), กรองช่วงราคา, กรองร่วมกับหมวดหมู่, กรณีราคาติดลบ, กรณีช่วงราคาขัดแย้ง, กรณีไม่พบสินค้า (Empty List `[]`), และรองรับภาษาไทย/Unicode (`"คีย์บอร์ดเกมมิ่ง"`, `"เมาส์ไร้สาย"`)
  * ผลการรัน `./gradlew.bat test` ผ่านครบ 100% ทุกชุดการทดสอบ

---

## 4. สิ่งที่ได้เรียนรู้
* **ประโยชน์ของ Layered Architecture:** เข้าใจความสำคัญของการแยก Business Logic ออกจาก Controller/Routing ทำให้สามารถเขียน Unit Test ตรวจสอบกฎทางธุรกิจได้อย่างรวดเร็วและครอบคลุม 100% โดยไม่ต้องเปิด HTTP Server
* **Many-to-One Relationship ใน Data Model:** การออกแบบโมเดลความสัมพันธ์ระหว่าง Category และ Product และการจัดการ Referential Integrity ในระดับ Service/Repository
* **Custom Domain Exceptions vs Generic Errors:** การตั้งชื่อ Exception ให้สะท้อนปัญหาในบริบทของธุรกิจจริง (เช่น `InsufficientStockException`, `NotFoundException`, `ValidationException`) ช่วยให้โค้ดอ่านเข้าใจง่าย และสื่อสารความผิดพลาดได้อย่างแม่นยำ
* **Global Exception Handling ด้วย StatusPages:** การลดความซ้ำซ้อนของการเขียน `try-catch` ใน Routing และการควบคุม Response Format ส่วนกลางให้สอดคล้องตามมาตรฐาน RESTful
* **Concurrency & Atomic State Management:** การรับมือกับ Concurrency ในระบบคลังสินค้า เพื่อป้องกันสต็อกติดลบและความผิดพลาดของข้อมูลเมื่อมี Request หลายตัวทำงานพร้อมกัน
* **การประมวลผลข้อมูลด้วย Kotlin Sequences:** การใช้ `asSequence()` ช่วยให้การกรองข้อมูลหลายขั้นตอนทำงานแบบ Lazy Evaluation ซึ่งช่วยประหยัด Memory และเพิ่มความเร็วเมื่อจัดการข้อมูลในหน่วยความจำ
* **Multi-Agent Collaboration ในการพัฒนาซอฟต์แวร์:** การแบ่งหน้าที่ระหว่าง Tech Lead (วางแผน/ประสานงาน), Developer (เขียนโค้ดและชุดทดสอบ) และ Reviewer/QA (Audit ตรวจสอบข้อผิดพลาดและรัน Test Suite) ช่วยยกระดับความถูกต้องและคุณภาพของโค้ดให้อยู่ในระดับ Production-Ready
