package com.example.routes

import com.example.model.AdjustStockRequest
import com.example.model.CreateProductRequest
import com.example.model.UpdateProductRequest
import com.example.model.ValidationException
import com.example.service.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(productService: ProductService) {
    route("/products") {
        get {
            val categoryId = call.request.queryParameters["categoryId"]?.let {
                it.toIntOrNull() ?: throw ValidationException("Invalid categoryId query parameter. Must be an integer.")
            }
            val products = productService.getAllProducts(categoryId)
            call.respond(HttpStatusCode.OK, products)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid product ID format. Must be an integer.")
            val product = productService.getProductById(id)
            call.respond(HttpStatusCode.OK, product)
        }

        post {
            val request = call.receive<CreateProductRequest>()
            val created = productService.createProduct(request)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid product ID format. Must be an integer.")
            val request = call.receive<UpdateProductRequest>()
            val updated = productService.updateProduct(id, request)
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid product ID format. Must be an integer.")
            productService.deleteProduct(id)
            call.respond(HttpStatusCode.NoContent, "")
        }

        post("/{id}/add-stock") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid product ID format. Must be an integer.")
            val request = call.receive<AdjustStockRequest>()
            val updated = productService.addStock(id, request.amount)
            call.respond(HttpStatusCode.OK, updated)
        }

        post("/{id}/reduce-stock") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ValidationException("Invalid product ID format. Must be an integer.")
            val request = call.receive<AdjustStockRequest>()
            val updated = productService.reduceStock(id, request.amount)
            call.respond(HttpStatusCode.OK, updated)
        }
    }
}
