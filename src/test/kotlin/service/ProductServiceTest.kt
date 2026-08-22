package com.example.service

import com.example.model.*
import com.example.repository.InMemoryCategoryRepository
import com.example.repository.InMemoryProductRepository
import kotlin.test.*

class ProductServiceTest {

    private lateinit var productRepository: InMemoryProductRepository
    private lateinit var categoryRepository: InMemoryCategoryRepository
    private lateinit var productService: ProductService

    @BeforeTest
    fun setUp() {
        productRepository = InMemoryProductRepository()
        categoryRepository = InMemoryCategoryRepository()
        productService = ProductService(productRepository, categoryRepository)

        // Seed a default category
        categoryRepository.create("Electronics") // ID = 1
    }

    @Test
    fun `test create product successfully`() {
        val request = CreateProductRequest(
            name = "Smartphone",
            description = "Latest flagship",
            price = 899.99,
            stockQuantity = 20,
            categoryId = 1
        )
        val product = productService.createProduct(request)

        assertEquals(1, product.id)
        assertEquals("Smartphone", product.name)
        assertEquals(899.99, product.price)
        assertEquals(20, product.stockQuantity)
        assertEquals(1, product.categoryId)
    }

    @Test
    fun `test create product with invalid data throws ValidationException`() {
        // Blank name
        assertFailsWith<ValidationException> {
            productService.createProduct(CreateProductRequest(name = "  ", price = 100.0, categoryId = 1))
        }

        // Negative price
        assertFailsWith<ValidationException> {
            productService.createProduct(CreateProductRequest(name = "Product", price = -5.0, categoryId = 1))
        }

        // Negative initial stock
        assertFailsWith<ValidationException> {
            productService.createProduct(CreateProductRequest(name = "Product", price = 10.0, stockQuantity = -1, categoryId = 1))
        }
    }

    @Test
    fun `test create product with non-existent category throws NotFoundException`() {
        val ex = assertFailsWith<NotFoundException> {
            productService.createProduct(CreateProductRequest(name = "Product", price = 10.0, categoryId = 999))
        }
        assertTrue(ex.message!!.contains("999"))
    }

    @Test
    fun `test add stock successfully`() {
        val created = productService.createProduct(
            CreateProductRequest(name = "Headphones", price = 50.0, stockQuantity = 10, categoryId = 1)
        )

        val updated = productService.addStock(created.id, 15)
        assertEquals(25, updated.stockQuantity)
    }

    @Test
    fun `test add stock with invalid amount throws ValidationException`() {
        val created = productService.createProduct(
            CreateProductRequest(name = "Headphones", price = 50.0, stockQuantity = 10, categoryId = 1)
        )

        assertFailsWith<ValidationException> {
            productService.addStock(created.id, 0)
        }
        assertFailsWith<ValidationException> {
            productService.addStock(created.id, -5)
        }
    }

    @Test
    fun `test reduce stock successfully`() {
        val created = productService.createProduct(
            CreateProductRequest(name = "Keyboard", price = 75.0, stockQuantity = 10, categoryId = 1)
        )

        val updated = productService.reduceStock(created.id, 4)
        assertEquals(6, updated.stockQuantity)
    }

    @Test
    fun `test reduce stock exceeding available quantity throws InsufficientStockException`() {
        val created = productService.createProduct(
            CreateProductRequest(name = "Monitor", price = 300.0, stockQuantity = 3, categoryId = 1)
        )

        val ex = assertFailsWith<InsufficientStockException> {
            productService.reduceStock(created.id, 5)
        }
        assertTrue(ex.message!!.contains("Insufficient stock", ignoreCase = true))

        // Ensure stock was not modified
        val fetched = productService.getProductById(created.id)
        assertEquals(3, fetched.stockQuantity)
    }

    @Test
    fun `test getAllProducts filtering by minPrice only`() {
        productService.createProduct(CreateProductRequest(name = "Item 100", price = 100.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Item 200", price = 200.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Item 300", price = 300.0, categoryId = 1))

        val results = productService.getAllProducts(minPrice = 200.0)
        assertEquals(2, results.size)
        assertEquals(listOf(200.0, 300.0), results.map { it.price })
    }

    @Test
    fun `test getAllProducts filtering by maxPrice only`() {
        productService.createProduct(CreateProductRequest(name = "Item 100", price = 100.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Item 200", price = 200.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Item 300", price = 300.0, categoryId = 1))

        val results = productService.getAllProducts(maxPrice = 200.0)
        assertEquals(2, results.size)
        assertEquals(listOf(100.0, 200.0), results.map { it.price })
    }

    @Test
    fun `test getAllProducts filtering by price range`() {
        productService.createProduct(CreateProductRequest(name = "Budget", price = 50.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Mid", price = 150.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "High", price = 500.0, categoryId = 1))

        val results = productService.getAllProducts(minPrice = 100.0, maxPrice = 200.0)
        assertEquals(1, results.size)
        assertEquals("Mid", results[0].name)
    }

    @Test
    fun `test getAllProducts filtering by categoryId and price range together`() {
        val cat2 = categoryRepository.create("Books")
        productService.createProduct(CreateProductRequest(name = "Tech Gadget", price = 150.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Tech Cable", price = 20.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Coding Book", price = 150.0, categoryId = cat2.id))

        val results = productService.getAllProducts(categoryId = 1, minPrice = 50.0, maxPrice = 200.0)
        assertEquals(1, results.size)
        assertEquals("Tech Gadget", results[0].name)
    }

    @Test
    fun `test getAllProducts with negative minPrice throws ValidationException`() {
        val ex = assertFailsWith<ValidationException> {
            productService.getAllProducts(minPrice = -10.0)
        }
        assertEquals("minPrice must be non-negative (received: -10.0)", ex.message)
    }

    @Test
    fun `test getAllProducts with negative maxPrice throws ValidationException`() {
        val ex = assertFailsWith<ValidationException> {
            productService.getAllProducts(maxPrice = -5.0)
        }
        assertEquals("maxPrice must be non-negative (received: -5.0)", ex.message)
    }

    @Test
    fun `test getAllProducts with minPrice greater than maxPrice throws ValidationException`() {
        val ex = assertFailsWith<ValidationException> {
            productService.getAllProducts(minPrice = 500.0, maxPrice = 100.0)
        }
        assertEquals("minPrice (500.0) cannot be greater than maxPrice (100.0)", ex.message)
    }

    @Test
    fun `test getAllProducts with non-existent categoryId throws NotFoundException`() {
        val ex = assertFailsWith<NotFoundException> {
            productService.getAllProducts(categoryId = 999, minPrice = 10.0, maxPrice = 100.0)
        }
        assertTrue(ex.message!!.contains("Category with id 999 not found"))
    }

    @Test
    fun `test getAllProducts with exact price match`() {
        productService.createProduct(CreateProductRequest(name = "Item 100", price = 100.0, categoryId = 1))
        productService.createProduct(CreateProductRequest(name = "Item 200", price = 200.0, categoryId = 1))

        val results = productService.getAllProducts(minPrice = 100.0, maxPrice = 100.0)
        assertEquals(1, results.size)
        assertEquals("Item 100", results[0].name)
    }

    @Test
    fun `test getAllProducts with no matching products returns empty list`() {
        productService.createProduct(CreateProductRequest(name = "Item 100", price = 100.0, categoryId = 1))

        val results = productService.getAllProducts(minPrice = 500.0, maxPrice = 1000.0)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `test getAllProducts with Thai unicode product names and price filtering`() {
        productService.createProduct(
            CreateProductRequest(name = "คีย์บอร์ดเกมมิ่ง", description = "มีไฟ RGB ภาษาไทย", price = 1290.0, categoryId = 1)
        )
        productService.createProduct(
            CreateProductRequest(name = "เมาส์ไร้สาย", description = "เซนเซอร์แม่นยำสูง", price = 590.0, categoryId = 1)
        )
        productService.createProduct(
            CreateProductRequest(name = "หูฟังบลูทูธ", description = "ตัดเสียงรบกวน", price = 2590.0, categoryId = 1)
        )

        val results = productService.getAllProducts(minPrice = 500.0, maxPrice = 1500.0)
        assertEquals(2, results.size)
        assertEquals(listOf("คีย์บอร์ดเกมมิ่ง", "เมาส์ไร้สาย"), results.map { it.name })
    }
}
