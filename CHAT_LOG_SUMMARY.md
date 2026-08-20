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
* **การปรับปรุงบันทึกสรุปผลการเรียนรู้:**
  * สั่งให้ AI อัปเดตไฟล์ `CHAT_LOG_SUMMARY.md` โดยคงหัวข้อเดิมไว้และสรุปเนื้อหาจากการสนทนาและการพัฒนาจริงในรอบนี้

---

## 2. AI ตอบผิด / น่าสงสัยตรงไหน
* **ปัญหา Overload Resolution Ambiguity ใน StatusPages.kt:**
  * ในขั้นตอนแรกที่มีการคอมไพล์โค้ด เกิด Error `Overload resolution ambiguity between candidates: class NotFoundException : DomainException vs class NotFoundException : Exception` เนื่องจากมีการใช้ Wildcard Import (`import io.ktor.server.plugins.*` ชนกับ `import com.example.model.*`) ทำให้ Ktor สับสนระหว่าง Exception ของระบบและ Custom Domain Exception ที่สร้างขึ้นเอง
  * **การแก้ไข:** ปรับเปลี่ยนการ Import ให้เป็นแบบ Explicit Class Names และเขียน Handler ดักจับแยกคลาสกันอย่างชัดเจน ส่งผลให้คอมไพล์ผ่านและทำงานได้อย่างสมบูรณ์
* **การจัดการ Dependency Version Catalog:**
  * โปรเจกต์ใช้ Gradle Version Catalog (`libs.versions.toml` และ `ktorLibs`) แต่ยังขาดการผูก Plugin `kotlin.plugin.serialization` และ `ktor-server-status-pages` ทาง AI จึงต้องเพิ่มการประกาศ plugin ใน `libs.versions.toml` และใส่ dependencies ใน `build.gradle.kts` ให้ครบถ้วน

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
* **การรวมศูนย์การจัดการ Error ด้วย StatusPages:**
  * ติดตั้ง `StatusPages` Plugin ดักจับ Custom Domain Exceptions ทั้งหมด แล้วแปลงเป็น HTTP Status Codes (`404 Not Found`, `400 Bad Request`, `409 Conflict`, `500 Internal Server Error`) และตอบกลับเป็น JSON `ErrorResponse` เป็นรูปแบบมาตรฐานเดียวกัน
* **การทดสอบอย่างครอบคลุมด้วย Automated Tests:**
  * สร้าง Unit Tests สำหรับ `RepositoryTest` (3 tests), `CategoryServiceTest` (7 tests), `ProductServiceTest` (7 tests)
  * สร้าง Integration Tests ใน `ServerTest` (2 tests) โดยใช้ Ktor `testApplication` จำลอง Full API Lifecycle
  * ผลการรัน `./gradlew.bat test` ผ่านครบ 100% ทั้ง 19 Test Cases

---

## 4. สิ่งที่ได้เรียนรู้
* **ประโยชน์ของ Layered Architecture:** เข้าใจความสำคัญของการแยก Business Logic ออกจาก Controller/Routing ทำให้สามารถเขียน Unit Test ตรวจสอบกฎทางธุรกิจได้อย่างรวดเร็วและครอบคลุม 100% โดยไม่ต้องเปิด HTTP Server
* **Many-to-One Relationship ใน Data Model:** การออกแบบโมเดลความสัมพันธ์ระหว่าง Category และ Product และการจัดการ Referential Integrity ในระดับ Service/Repository
* **Custom Domain Exceptions vs Generic Errors:** การตั้งชื่อ Exception ให้สะท้อนปัญหาในบริบทของธุรกิจจริง (เช่น `InsufficientStockException`, `NotFoundException`) ช่วยให้โค้ดอ่านเข้าใจง่าย และสื่อสารความผิดพลาดได้อย่างแม่นยำ
* **Global Exception Handling ด้วย StatusPages:** การลดความซ้ำซ้อนของการเขียน `try-catch` ใน Routing และการควบคุม Response Format ส่วนกลางให้สอดคล้องตามมาตรฐาน RESTful
* **Concurrency & Atomic State Management:** การรับมือกับ Concurrency ในระบบคลังสินค้า เพื่อป้องกันสต็อกติดลบและความผิดพลาดของข้อมูลเมื่อมี Request หลายตัวทำงานพร้อมกัน
* **Integration Testing with Ktor TestHost:** การเขียน Integration Test ที่สมบูรณ์แบบด้วย `testApplication` และ `ContentNegotiation Client` เพื่อจำลองการทำงานจริงของ RESTful API
