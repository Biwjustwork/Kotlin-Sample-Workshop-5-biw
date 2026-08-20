package com.example.repository

import com.example.model.InsufficientStockException
import kotlin.test.*

class RepositoryTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var productRepository: ProductRepository

    @BeforeTest
    fun setUp() {
        categoryRepository = InMemoryCategoryRepository()
        productRepository = InMemoryProductRepository()
    }

    @Test
    fun `test category repository CRUD operations`() {
        val cat1 = categoryRepository.create("Electronics")
        assertEquals(1, cat1.id)
        assertEquals("Electronics", cat1.name)

        val fetched = categoryRepository.getById(cat1.id)
        assertNotNull(fetched)
        assertEquals("Electronics", fetched.name)

        val updated = categoryRepository.update(cat1.id, "Home Electronics")
        assertNotNull(updated)
        assertEquals("Home Electronics", updated.name)

        assertTrue(categoryRepository.existsByName("Home Electronics"))
        assertFalse(categoryRepository.existsByName("NonExistent"))

        val deleted = categoryRepository.delete(cat1.id)
        assertTrue(deleted)
        assertNull(categoryRepository.getById(cat1.id))
    }

    @Test
    fun `test product repository CRUD operations and filtering`() {
        val prod1 = productRepository.create("Laptop", "Gaming Laptop", 1500.0, 10, 1)
        val prod2 = productRepository.create("Book", "Sci-Fi", 20.0, 50, 2)

        val allProducts = productRepository.getAll()
        assertEquals(2, allProducts.size)

        val cat1Products = productRepository.getAll(categoryId = 1)
        assertEquals(1, cat1Products.size)
        assertEquals("Laptop", cat1Products[0].name)

        val updated = productRepository.update(prod1.id, "Ultra Laptop", "Workstation", 1800.0, 1)
        assertNotNull(updated)
        assertEquals("Ultra Laptop", updated.name)
        assertEquals(1800.0, updated.price)

        assertTrue(productRepository.existsByCategoryId(1))
        assertTrue(productRepository.delete(prod1.id))
        assertFalse(productRepository.existsByCategoryId(1))
    }

    @Test
    fun `test product stock updates and atomic safety`() {
        val prod = productRepository.create("Mouse", "Wireless", 25.0, 10, 1)

        val afterAdd = productRepository.updateStock(prod.id, 5)
        assertEquals(15, afterAdd?.stockQuantity)

        val afterReduce = productRepository.updateStock(prod.id, -10)
        assertEquals(5, afterReduce?.stockQuantity)

        assertFailsWith<InsufficientStockException> {
            productRepository.updateStock(prod.id, -10)
        }

        // Verify stock remains intact after failed deduction
        val current = productRepository.getById(prod.id)
        assertEquals(5, current?.stockQuantity)
    }
}
