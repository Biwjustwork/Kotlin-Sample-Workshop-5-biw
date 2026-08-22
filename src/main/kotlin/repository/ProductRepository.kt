package com.example.repository

import com.example.model.InsufficientStockException
import com.example.model.Product
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Data access abstraction for Product entity.
 */
interface ProductRepository {
    fun getAll(categoryId: Int? = null, minPrice: Double? = null, maxPrice: Double? = null): List<Product>
    fun getById(id: Int): Product?
    fun create(name: String, description: String?, price: Double, stockQuantity: Int, categoryId: Int): Product
    fun update(id: Int, name: String, description: String?, price: Double, categoryId: Int): Product?
    fun delete(id: Int): Boolean
    fun existsByCategoryId(categoryId: Int): Boolean
    fun updateStock(id: Int, delta: Int): Product?
    fun clear()
}

/**
 * Thread-safe In-Memory implementation of ProductRepository.
 */
class InMemoryProductRepository : ProductRepository {
    private val products = ConcurrentHashMap<Int, Product>()
    private val idCounter = AtomicInteger(1)

    override fun getAll(categoryId: Int?, minPrice: Double?, maxPrice: Double?): List<Product> {
        return products.values
            .asSequence()
            .filter { categoryId == null || it.categoryId == categoryId }
            .filter { minPrice == null || it.price >= minPrice }
            .filter { maxPrice == null || it.price <= maxPrice }
            .sortedBy { it.id }
            .toList()
    }

    override fun getById(id: Int): Product? {
        return products[id]
    }

    override fun create(
        name: String,
        description: String?,
        price: Double,
        stockQuantity: Int,
        categoryId: Int
    ): Product {
        val id = idCounter.getAndIncrement()
        val product = Product(
            id = id,
            name = name.trim(),
            description = description?.trim(),
            price = price,
            stockQuantity = stockQuantity,
            categoryId = categoryId
        )
        products[id] = product
        return product
    }

    override fun update(
        id: Int,
        name: String,
        description: String?,
        price: Double,
        categoryId: Int
    ): Product? {
        return products.computeIfPresent(id) { _, current ->
            current.copy(
                name = name.trim(),
                description = description?.trim(),
                price = price,
                categoryId = categoryId
            )
        }
    }

    override fun delete(id: Int): Boolean {
        return products.remove(id) != null
    }

    override fun existsByCategoryId(categoryId: Int): Boolean {
        return products.values.any { it.categoryId == categoryId }
    }

    /**
     * Atomically modifies the stock of a product by delta.
     * Prevents stock from dropping below zero (Data Integrity / Concurrency safety).
     */
    override fun updateStock(id: Int, delta: Int): Product? {
        var error: InsufficientStockException? = null
        val updated = products.computeIfPresent(id) { _, current ->
            val newStock = current.stockQuantity + delta
            if (newStock < 0) {
                error = InsufficientStockException(
                    "Insufficient stock for product ID $id. Current stock: ${current.stockQuantity}, requested reduction: ${-delta}"
                )
                current // do not modify
            } else {
                current.copy(stockQuantity = newStock)
            }
        }
        error?.let { throw it }
        return updated
    }

    override fun clear() {
        products.clear()
        idCounter.set(1)
    }
}
