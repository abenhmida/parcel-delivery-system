package com.krizaldis.parceldelivery.events

open class ParcelEventProcessingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class RetryableParcelEventException(
    message: String,
    cause: Throwable? = null,
) : ParcelEventProcessingException(message, cause)

class NonRetryableParcelEventException(
    message: String,
    cause: Throwable? = null,
) : ParcelEventProcessingException(message, cause)
