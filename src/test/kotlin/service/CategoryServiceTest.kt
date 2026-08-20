package com.example.service

import com.example.model.*
import com.example.repository.InMemoryCategoryRepository
import com.example.repository.InMemoryProductRepository
import kotlin.test.*

class CategoryServiceTest {

    private lateinit var categoryRepository: InMemoryCategoryRepository
    private lateinit var productRepository: InMemoryProductRepository
    private lateinit var categoryService: CategoryService

    @BeforeTest
    fun setUp() {
        categoryRepository = InMemoryCategoryRepository()
        productRepository = InMemoryProductRepository()
        categoryService = CategoryService(categoryRepository, productRepository)
    }

    @Test
    fun `test create category successfully`() {
        val category = categoryService.createCategory(CreateCategoryRequest("Books"))
        assertEquals(1, category.id)
        assertEquals("Books", category.name)
    }

    @Test
    fun `test create category with blank name throws ValidationException`() {
        val ex = assertFailsWith<ValidationException> {
            categoryService.createCategory(CreateCategoryRequest("   "))
        }
        assertTrue(ex.message!!.contains("blank", ignoreCase = true))
    }

    @Test
    fun `test create category with duplicate name throws ConflictException`() {
        categoryService.createCategory(CreateCategoryRequest("Clothing"))
        val ex = assertFailsWith<ConflictException> {
            categoryService.createCategory(CreateCategoryRequest("Clothing"))
        }
        assertTrue(ex.message!!.contains("already exists", ignoreCase = true))
    }

    @Test
    fun `test getCategoryById not found throws NotFoundException`() {
        val ex = assertFailsWith<NotFoundException> {
            categoryService.getCategoryById(999)
        }
        assertTrue(ex.message!!.contains("999"))
    }

    @Test
    fun `test update category successfully`() {
        val created = categoryService.createCategory(CreateCategoryRequest("Old Name"))
        val updated = categoryService.updateCategory(created.id, UpdateCategoryRequest("New Name"))
        assertEquals("New Name", updated.name)
    }

    @Test
    fun `test delete category throws ConflictException when products are assigned`() {
        val cat = categoryService.createCategory(CreateCategoryRequest("Appliances"))
        productRepository.create("Microwave", "Kitchen appliance", 120.0, 5, cat.id)

        val ex = assertFailsWith<ConflictException> {
            categoryService.deleteCategory(cat.id)
        }
        assertTrue(ex.message!!.contains("active products", ignoreCase = true))
    }

    @Test
    fun `test delete category successfully when no products assigned`() {
        val cat = categoryService.createCategory(CreateCategoryRequest("Stationery"))
        categoryService.deleteCategory(cat.id)
        assertEquals(0, categoryService.getAllCategories().size)
    }
}
