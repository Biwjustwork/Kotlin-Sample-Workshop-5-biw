package com.example.model

import kotlinx.serialization.Serializable

/**
 * Base class for all domain-specific business exceptions.
 */
sealed class DomainException(message: String) : RuntimeException(message)

/**
 * Thrown when an entity (Category, Product) cannot be found by its identifier.
 */
class NotFoundException(message: String) : DomainException(message)

/**
 * Thrown when input data fails domain validation rules.
 */
class ValidationException(message: String) : DomainException(message)

/**
 * Thrown when an operation violates state constraints (e.g. deleting a category that has products).
 */
class ConflictException(message: String) : DomainException(message)

/**
 * Thrown when trying to reduce stock below zero.
 */
class InsufficientStockException(message: String) : DomainException(message)

/**
 * Standard error response format sent to HTTP clients.
 */
@Serializable
data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
