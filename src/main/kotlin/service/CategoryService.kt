package com.example.service

import com.example.model.*
import com.example.repository.CategoryRepository
import com.example.repository.ProductRepository

/**
 * Service handling business rules for Category entities.
 */
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) {
    fun getAllCategories(): List<Category> {
        return categoryRepository.getAll()
    }

    fun getCategoryById(id: Int): Category {
        return categoryRepository.getById(id)
            ?: throw NotFoundException("Category with id $id not found")
    }

    fun createCategory(request: CreateCategoryRequest): Category {
        val trimmedName = request.name.trim()
        if (trimmedName.isBlank()) {
            throw ValidationException("Category name cannot be blank")
        }
        if (categoryRepository.existsByName(trimmedName)) {
            throw ConflictException("Category with name '$trimmedName' already exists")
        }
        return categoryRepository.create(trimmedName)
    }

    fun updateCategory(id: Int, request: UpdateCategoryRequest): Category {
        val trimmedName = request.name.trim()
        if (trimmedName.isBlank()) {
            throw ValidationException("Category name cannot be blank")
        }
        if (!categoryRepository.existsById(id)) {
            throw NotFoundException("Category with id $id not found")
        }
        if (categoryRepository.existsByName(trimmedName, excludeId = id)) {
            throw ConflictException("Category with name '$trimmedName' already exists")
        }
        return categoryRepository.update(id, trimmedName)
            ?: throw NotFoundException("Category with id $id not found")
    }

    fun deleteCategory(id: Int) {
        if (!categoryRepository.existsById(id)) {
            throw NotFoundException("Category with id $id not found")
        }
        if (productRepository.existsByCategoryId(id)) {
            throw ConflictException("Cannot delete category with id $id because active products are assigned to it")
        }
        val deleted = categoryRepository.delete(id)
        if (!deleted) {
            throw NotFoundException("Category with id $id not found")
        }
    }
}
