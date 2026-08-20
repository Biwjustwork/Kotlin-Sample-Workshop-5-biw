package com.example

import com.example.model.ConflictException
import com.example.model.ErrorResponse
import com.example.model.InsufficientStockException
import com.example.model.NotFoundException
import com.example.model.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    status = HttpStatusCode.NotFound.value,
                    message = cause.message ?: "Resource not found"
                )
            )
        }

        exception<io.ktor.server.plugins.NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    status = HttpStatusCode.NotFound.value,
                    message = cause.message ?: "Resource not found"
                )
            )
        }

        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = cause.message ?: "Validation failed"
                )
            )
        }

        exception<InsufficientStockException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = cause.message ?: "Insufficient stock"
                )
            )
        }

        exception<ConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(
                    status = HttpStatusCode.Conflict.value,
                    message = cause.message ?: "Resource conflict"
                )
            )
        }

        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = cause.message ?: "Malformed request payload"
                )
            )
        }

        exception<SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = "Invalid JSON data: ${cause.message}"
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = cause.message ?: "Illegal argument"
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    message = cause.message ?: "An unexpected internal server error occurred"
                )
            )
        }
    }
}
