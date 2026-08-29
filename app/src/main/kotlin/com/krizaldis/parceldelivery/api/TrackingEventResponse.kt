package com.krizaldis.parceldelivery.api

import java.time.Instant

data class TrackingEventResponse(
    val status: String,
    val occurredAt: Instant,
)
