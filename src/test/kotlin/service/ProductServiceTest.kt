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
}
