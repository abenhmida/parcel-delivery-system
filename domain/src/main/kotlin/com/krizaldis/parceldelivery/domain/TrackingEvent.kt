package com.krizaldis.parceldelivery.domain

import java.time.Instant
import java.util.UUID

data class TrackingEvent(
    val parcelId: UUID,
    val status: ParcelStatus,
    val occurredAt: Instant,
)
