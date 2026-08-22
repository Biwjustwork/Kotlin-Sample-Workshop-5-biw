package com.example.service

import com.example.model.*
import com.example.repository.CategoryRepository
import com.example.repository.ProductRepository

/**
 * Service handling business rules and inventory operations for Product entities.
 */
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {
    fun getAllProducts(
        categoryId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ): List<Product> {
        if (minPrice != null && minPrice < 0.0) {
            throw ValidationException("minPrice must be non-negative (received: $minPrice)")
        }
        if (maxPrice != null && maxPrice < 0.0) {
            throw ValidationException("maxPrice must be non-negative (received: $maxPrice)")
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw ValidationException("minPrice ($minPrice) cannot be greater than maxPrice ($maxPrice)")
        }
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw NotFoundException("Category with id $categoryId not found")
        }
        return productRepository.getAll(categoryId, minPrice, maxPrice)
    }

    fun getProductById(id: Int): Product {
        return productRepository.getById(id)
            ?: throw NotFoundException("Product with id $id not found")
    }

    fun createProduct(request: CreateProductRequest): Product {
        val trimmedName = request.name.trim()
        if (trimmedName.isBlank()) {
            throw ValidationException("Product name cannot be blank")
        }
        if (request.price < 0.0) {
            throw ValidationException("Product price must be non-negative (received: ${request.price})")
        }
        if (request.stockQuantity < 0) {
            throw ValidationException("Product initial stock must be non-negative (received: ${request.stockQuantity})")
        }
        if (!categoryRepository.existsById(request.categoryId)) {
            throw NotFoundException("Category with id ${request.categoryId} not found")
        }

        return productRepository.create(
            name = trimmedName,
            description = request.description?.trim(),
            price = request.price,
            stockQuantity = request.stockQuantity,
            categoryId = request.categoryId
        )
    }

    fun updateProduct(id: Int, request: UpdateProductRequest): Product {
        val trimmedName = request.name.trim()
        if (trimmedName.isBlank()) {
            throw ValidationException("Product name cannot be blank")
        }
        if (request.price < 0.0) {
            throw ValidationException("Product price must be non-negative (received: ${request.price})")
        }
        if (!productRepository.getById(id).let { it != null }) {
            throw NotFoundException("Product with id $id not found")
        }
        if (!categoryRepository.existsById(request.categoryId)) {
            throw NotFoundException("Category with id ${request.categoryId} not found")
        }

        return productRepository.update(
            id = id,
            name = trimmedName,
            description = request.description?.trim(),
            price = request.price,
            categoryId = request.categoryId
        ) ?: throw NotFoundException("Product with id $id not found")
    }

    fun deleteProduct(id: Int) {
        if (productRepository.getById(id) == null) {
            throw NotFoundException("Product with id $id not found")
        }
        val deleted = productRepository.delete(id)
        if (!deleted) {
            throw NotFoundException("Product with id $id not found")
        }
    }

    fun addStock(id: Int, amount: Int): Product {
        if (amount <= 0) {
            throw ValidationException("Stock addition amount must be greater than 0 (received: $amount)")
        }
        if (productRepository.getById(id) == null) {
            throw NotFoundException("Product with id $id not found")
        }
        return productRepository.updateStock(id, amount)
            ?: throw NotFoundException("Product with id $id not found")
    }

    fun reduceStock(id: Int, amount: Int): Product {
        if (amount <= 0) {
            throw ValidationException("Stock reduction amount must be greater than 0 (received: $amount)")
        }
        if (productRepository.getById(id) == null) {
            throw NotFoundException("Product with id $id not found")
        }
        return productRepository.updateStock(id, -amount)
            ?: throw NotFoundException("Product with id $id not found")
    }
}
