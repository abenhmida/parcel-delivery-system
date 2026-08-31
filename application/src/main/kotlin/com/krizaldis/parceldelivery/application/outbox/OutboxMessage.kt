package com.krizaldis.parceldelivery.application.outbox

import java.time.Instant
import java.util.UUID

data class OutboxMessage(
    val id: UUID,
    val aggregateId: UUID,
    val eventType: String,
    val payload: String,
    val createdAt: Instant,
    val publishedAt: Instant?,
    val attempts: Int,
    val lastError: String?,
)
