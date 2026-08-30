package com.krizaldis.parceldelivery.api

import com.example.parceldelivery.application.ParcelNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidRequest(exception: IllegalArgumentException) = ApiError("INVALID_REQUEST", exception.message ?: "Invalid request")

    @ExceptionHandler(ParcelNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
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
    @ResponseStatus(HttpStatus.CONFLICT)
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
