package com.krizaldis.parceldelivery.rest

import com.krizaldis.parceldelivery.exceptions.ParcelNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ParcelNotFoundException::class)
    fun notFound(exception: ParcelNotFoundException): ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiError(
                    code = "PARCEL_NOT_FOUND",
                    message = exception.message ?: "Parcel not found",
                ),
            )

    @ExceptionHandler(IllegalStateException::class)
    fun invalidTransition(exception: IllegalStateException): ResponseEntity<ApiError> =
        ResponseEntity
            .badRequest()
            .body(
                ApiError(
                    code = "INVALID_STATE_TRANSITION",
                    message = exception.message ?: "Invalid state transition",
                ),
            )
}

data class ApiError(
    val code: String,
    val message: String,
)
