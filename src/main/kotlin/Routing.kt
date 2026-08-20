package com.example

import com.example.repository.CategoryRepository
import com.example.repository.InMemoryCategoryRepository
import com.example.repository.InMemoryProductRepository
import com.example.repository.ProductRepository
import com.example.routes.categoryRoutes
import com.example.routes.productRoutes
import com.example.service.CategoryService
import com.example.service.ProductService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Singleton In-Memory Repositories for the application lifetime
val defaultCategoryRepository: CategoryRepository = InMemoryCategoryRepository()
val defaultProductRepository: ProductRepository = InMemoryProductRepository()

val defaultCategoryService = CategoryService(defaultCategoryRepository, defaultProductRepository)
val defaultProductService = ProductService(defaultProductRepository, defaultCategoryRepository)

fun Application.configureRouting(
    categoryService: CategoryService = defaultCategoryService,
    productService: ProductService = defaultProductService
) {
    routing {
        get("/") {
            call.respondText("Welcome to Simple E-commerce Inventory API!")
        }
        categoryRoutes(categoryService)
        productRoutes(productService)
    }
}