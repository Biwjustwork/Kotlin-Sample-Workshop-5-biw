package com.example.repository

import com.example.model.Category
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Data access abstraction for Category entity.
 */
interface CategoryRepository {
    fun getAll(): List<Category>
    fun getById(id: Int): Category?
    fun create(name: String): Category
    fun update(id: Int, name: String): Category?
    fun delete(id: Int): Boolean
    fun existsById(id: Int): Boolean
    fun existsByName(name: String, excludeId: Int? = null): Boolean
    fun clear()
}

/**
 * Thread-safe In-Memory implementation of CategoryRepository.
 */
class InMemoryCategoryRepository : CategoryRepository {
    private val categories = ConcurrentHashMap<Int, Category>()
    private val idCounter = AtomicInteger(1)

    override fun getAll(): List<Category> {
        return categories.values.sortedBy { it.id }
    }

    override fun getById(id: Int): Category? {
        return categories[id]
    }

    override fun create(name: String): Category {
        val id = idCounter.getAndIncrement()
        val category = Category(id = id, name = name.trim())
        categories[id] = category
        return category
    }

    override fun update(id: Int, name: String): Category? {
        return categories.computeIfPresent(id) { _, _ ->
            Category(id = id, name = name.trim())
        }
    }

    override fun delete(id: Int): Boolean {
        return categories.remove(id) != null
    }

    override fun existsById(id: Int): Boolean {
        return categories.containsKey(id)
    }

    override fun existsByName(name: String, excludeId: Int?): Boolean {
        val normalized = name.trim().lowercase()
        return categories.values.any { it.name.trim().lowercase() == normalized && it.id != excludeId }
    }

    override fun clear() {
        categories.clear()
        idCounter.set(1)
    }
}
