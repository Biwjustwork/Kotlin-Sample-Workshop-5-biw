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
    fun `test product repository price filtering`() {
        productRepository.create("Keyboard", "Mechanical", 50.0, 10, 1)
        productRepository.create("Monitor", "4K HDR", 300.0, 5, 1)
        productRepository.create("Mouse", "Wireless", 25.0, 20, 1)
        productRepository.create("Novel", "Fantasy", 15.0, 100, 2)
        productRepository.create("Desk", "Standing", 300.0, 2, 2)

        // minPrice filter
        val min100 = productRepository.getAll(minPrice = 100.0)
        assertEquals(2, min100.size)
        assertTrue(min100.all { it.price >= 100.0 })

        // maxPrice filter
        val max30 = productRepository.getAll(maxPrice = 30.0)
        assertEquals(2, max30.size)
        assertTrue(max30.all { it.price <= 30.0 })

        // minPrice and maxPrice range filter
        val range25to300 = productRepository.getAll(minPrice = 25.0, maxPrice = 300.0)
        assertEquals(4, range25to300.size)

        // categoryId and price range combined
        val cat1Range = productRepository.getAll(categoryId = 1, minPrice = 30.0, maxPrice = 300.0)
        assertEquals(2, cat1Range.size)
        assertEquals(listOf("Keyboard", "Monitor"), cat1Range.map { it.name })

        // exact match minPrice == maxPrice
        val exact300 = productRepository.getAll(minPrice = 300.0, maxPrice = 300.0)
        assertEquals(2, exact300.size)

        // no match
        val noMatch = productRepository.getAll(minPrice = 1000.0)
        assertTrue(noMatch.isEmpty())
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
