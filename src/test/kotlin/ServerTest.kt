package com.example

import com.example.model.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.*

class ServerTest {

    @BeforeTest
    fun cleanUp() {
        defaultCategoryRepository.clear()
        defaultProductRepository.clear()
    }

    @Test
    fun `test root endpoint`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `test complete category and product inventory lifecycle`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }
        val jsonClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // 1. Create Category
        val catResponse = jsonClient.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(CreateCategoryRequest("Gaming Devices"))
        }
        assertEquals(HttpStatusCode.Created, catResponse.status)
        val createdCat = catResponse.body<Category>()
        assertEquals("Gaming Devices", createdCat.name)

        // 2. Create Product
        val prodResponse = jsonClient.post("/products") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateProductRequest(
                    name = "Handheld Console",
                    description = "Portable OLED console",
                    price = 349.99,
                    stockQuantity = 5,
                    categoryId = createdCat.id
                )
            )
        }
        assertEquals(HttpStatusCode.Created, prodResponse.status)
        val createdProd = prodResponse.body<Product>()
        assertEquals(5, createdProd.stockQuantity)

        // 3. Add Stock (+10)
        val addStockResponse = jsonClient.post("/products/${createdProd.id}/add-stock") {
            contentType(ContentType.Application.Json)
            setBody(AdjustStockRequest(amount = 10))
        }
        assertEquals(HttpStatusCode.OK, addStockResponse.status)
        val prodAfterAdd = addStockResponse.body<Product>()
        assertEquals(15, prodAfterAdd.stockQuantity)

        // 4. Reduce Stock (-5)
        val reduceStockResponse = jsonClient.post("/products/${createdProd.id}/reduce-stock") {
            contentType(ContentType.Application.Json)
            setBody(AdjustStockRequest(amount = 5))
        }
        assertEquals(HttpStatusCode.OK, reduceStockResponse.status)
        val prodAfterReduce = reduceStockResponse.body<Product>()
        assertEquals(10, prodAfterReduce.stockQuantity)

        // 5. Reduce Stock Exceeding Available (-20) -> Should fail with 400 Bad Request
        val failReduceResponse = jsonClient.post("/products/${createdProd.id}/reduce-stock") {
            contentType(ContentType.Application.Json)
            setBody(AdjustStockRequest(amount = 20))
        }
        assertEquals(HttpStatusCode.BadRequest, failReduceResponse.status)
        val errorBody = failReduceResponse.body<ErrorResponse>()
        assertTrue(errorBody.message.contains("Insufficient stock", ignoreCase = true))

        // 6. Attempt to delete category with active products -> Should fail with 409 Conflict
        val failDeleteCatResponse = jsonClient.delete("/categories/${createdCat.id}")
        assertEquals(HttpStatusCode.Conflict, failDeleteCatResponse.status)

        // 7. Delete product -> 204 No Content
        val deleteProdResponse = jsonClient.delete("/products/${createdProd.id}")
        assertEquals(HttpStatusCode.NoContent, deleteProdResponse.status)

        // 8. Delete category now -> 204 No Content
        val deleteCatResponse = jsonClient.delete("/categories/${createdCat.id}")
        assertEquals(HttpStatusCode.NoContent, deleteCatResponse.status)
    }
}
