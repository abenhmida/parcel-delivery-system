package com.krizaldis.parceldelivery.application.outbox

import java.util.UUID

interface OutboxRepository {
    fun insert(message: OutboxMessage)

    fun findPending(limit: Int): List<OutboxMessage>

    fun markPublished(id: UUID)

    fun markFailed(
        id: UUID,
        error: String,
    )
}
