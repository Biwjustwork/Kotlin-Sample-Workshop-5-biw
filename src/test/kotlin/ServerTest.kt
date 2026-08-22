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

    @Test
    fun `test get products price filtering and validation via HTTP`() = testApplication {
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

        // Setup category and products
        val cat = defaultCategoryRepository.create("Gadgets")
        defaultProductRepository.create("Budget Earbuds", "Basic", 50.0, 10, cat.id)
        defaultProductRepository.create("Mid-tier Speaker", "Bluetooth", 250.0, 10, cat.id)
        defaultProductRepository.create("Flagship Phone", "Premium", 800.0, 5, cat.id)

        // 1. GET /products?minPrice=100&maxPrice=500 -> Expect Mid-tier Speaker
        val rangeResponse = jsonClient.get("/products?minPrice=100&maxPrice=500")
        assertEquals(HttpStatusCode.OK, rangeResponse.status)
        val filtered = rangeResponse.body<List<Product>>()
        assertEquals(1, filtered.size)
        assertEquals("Mid-tier Speaker", filtered[0].name)
        assertEquals(250.0, filtered[0].price)

        // 2. GET /products?minPrice=-10 -> 400 Bad Request
        val negativeMinResponse = jsonClient.get("/products?minPrice=-10")
        assertEquals(HttpStatusCode.BadRequest, negativeMinResponse.status)
        val negativeMinError = negativeMinResponse.body<ErrorResponse>()
        assertTrue(negativeMinError.message.contains("minPrice must be non-negative"))

        // 3. GET /products?minPrice=500&maxPrice=100 -> 400 Bad Request
        val invalidRangeResponse = jsonClient.get("/products?minPrice=500&maxPrice=100")
        assertEquals(HttpStatusCode.BadRequest, invalidRangeResponse.status)
        val invalidRangeError = invalidRangeResponse.body<ErrorResponse>()
        assertTrue(invalidRangeError.message.contains("cannot be greater than maxPrice"))

        // 4. GET /products?minPrice=abc -> 400 Bad Request
        val invalidMinFormatResponse = jsonClient.get("/products?minPrice=abc")
        assertEquals(HttpStatusCode.BadRequest, invalidMinFormatResponse.status)
        val invalidMinFormatError = invalidMinFormatResponse.body<ErrorResponse>()
        assertTrue(invalidMinFormatError.message.contains("Invalid minPrice query parameter"))

        // 5. GET /products?maxPrice=xyz -> 400 Bad Request
        val invalidMaxFormatResponse = jsonClient.get("/products?maxPrice=xyz")
        assertEquals(HttpStatusCode.BadRequest, invalidMaxFormatResponse.status)
        val invalidMaxFormatError = invalidMaxFormatResponse.body<ErrorResponse>()
        assertTrue(invalidMaxFormatError.message.contains("Invalid maxPrice query parameter"))
    }
}
