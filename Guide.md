kotlin guide

**ข้อกำหนดร่วมสำหรับทุกโครงงาน:**

- **ภาษาและเฟรมเวิร์ก:** Kotlin และ Ktor 
- **API:** ต้องเป็น RESTful API ที่มีการออกแบบที่ดี ใช้ HTTP Verbs (GET, POST, PUT, DELETE) และ HTTP Status Codes ที่ถูกต้องตามหลักสากล
- **การจัดการข้อมูล:** ใช้ kotlinx.serialization สำหรับการแปลงข้อมูล JSON 3
- **การทดสอบ:** มี Unit Test สำหรับส่วนของ Business Logic และ Repository เป็นอย่างน้อย 5

**1. Simple E-commerce Inventory API**

- **คำอธิบาย:** สร้าง API สำหรับจัดการคลังสินค้าของร้านค้าออนไลน์ขนาดเล็ก ประกอบด้วยการจัดการหมวดหมู่สินค้า, ตัวสินค้า, และจำนวนสต็อก
- **ฟังก์ชันหลัก:**
- CRUD สำหรับ Categories (id, name)
- CRUD สำหรับ Products (id, name, description, price, stockQuantity) ซึ่งมีความสัมพันธ์กับ Categories
- Endpoint สำหรับการปรับปรุงสต็อกสินค้า (เช่น POST /products/{id}/add-stock)
- Business Logic เพื่อป้องกันไม่ให้จำนวนสต็อกสินค้าติดลบ

**แนวคิดที่ได้ฝึกฝน:** การออกแบบ Data Model ที่มีความสัมพันธ์แบบกลุ่มต่อหนึ่ง (Many-to-One) 2, การเขียน Business Logic ที่ซับซ้อนขึ้นใน Service/Repository Layer, การจัดการ Transaction ในฐานข้อมูลเพื่อความถูกต้องของข้อมูล (Data Integrity)