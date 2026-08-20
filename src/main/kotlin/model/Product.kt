package com.example.model

import kotlinx.serialization.Serializable

/**
 * Domain entity representing a product in the inventory.
 * Has a Many-to-One relationship with Category via categoryId.
 */
@Serializable
data class Product(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stockQuantity: Int,
    val categoryId: Int
)

/**
 * DTO for creating a new Product.
 */
@Serializable
data class CreateProductRequest(
    val name: String,
    val description: String? = null,
    val price: Double,
    val stockQuantity: Int = 0,
    val categoryId: Int
)

/**
 * DTO for updating an existing Product's information.
 */
@Serializable
data class UpdateProductRequest(
    val name: String,
    val description: String? = null,
    val price: Double,
    val categoryId: Int
)

/**
 * DTO for adding or reducing stock quantity.
 */
@Serializable
data class AdjustStockRequest(
    val amount: Int
)
