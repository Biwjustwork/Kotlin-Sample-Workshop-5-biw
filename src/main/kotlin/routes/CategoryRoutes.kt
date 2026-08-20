package com.example.routes

import com.example.model.CreateCategoryRequest
import com.example.model.UpdateCategoryRequest
import com.example.model.ValidationException
import com.example.service.CategoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(categoryService: CategoryService) {
    route("/categories") {
        get {
            val categories = categoryService.getAllCategories()
            call.respond(HttpStatusCode.OK, categories)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid category ID format. Must be an integer.")
            val category = categoryService.getCategoryById(id)
            call.respond(HttpStatusCode.OK, category)
        }

        post {
            val request = call.receive<CreateCategoryRequest>()
            val created = categoryService.createCategory(request)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid category ID format. Must be an integer.")
            val request = call.receive<UpdateCategoryRequest>()
            val updated = categoryService.updateCategory(id, request)
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid category ID format. Must be an integer.")
            categoryService.deleteCategory(id)
            call.respond(HttpStatusCode.NoContent, "")
        }
    }
}
