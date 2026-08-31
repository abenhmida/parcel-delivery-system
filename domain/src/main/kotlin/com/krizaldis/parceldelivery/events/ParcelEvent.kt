package com.krizaldis.parceldelivery.events

import java.time.Instant
import java.util.UUID

data class ParcelEvent(
    val eventId: UUID,
    val parcelId: UUID,
    val trackingNumber: String,
    val type: ParcelEventType,
    val occurredAt: Instant,
)
