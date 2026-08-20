package com.example.model

import kotlinx.serialization.Serializable

/**
 * Domain entity representing a product category.
 */
@Serializable
data class Category(
    val id: Int,
    val name: String
)

/**
 * DTO for creating a new Category.
 */
@Serializable
data class CreateCategoryRequest(
    val name: String
)

/**
 * DTO for updating an existing Category.
 */
@Serializable
data class UpdateCategoryRequest(
    val name: String
)
