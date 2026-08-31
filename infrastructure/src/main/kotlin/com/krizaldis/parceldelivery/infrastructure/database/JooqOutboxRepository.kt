package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.application.outbox.OutboxMessage
import com.krizaldis.parceldelivery.application.outbox.OutboxRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqOutboxRepository(
    private val dsl: DSLContext,
) : OutboxRepository {
    override fun insert(message: OutboxMessage) {
        dsl
            .insertInto(JooqSchema.OUTBOX_MESSAGES)
            .set(
                JooqSchema.OUTBOX_ID,
                message.id,
            ).set(
                JooqSchema.OUTBOX_AGGREGATE_ID,
                message.aggregateId,
            ).set(
                JooqSchema.OUTBOX_EVENT_TYPE,
                message.eventType,
            ).set(
                JooqSchema.OUTBOX_PAYLOAD,
                message.payload,
            ).set(
                JooqSchema.OUTBOX_CREATED_AT,
                message.createdAt.toOffsetDateTime(),
            ).set(
                JooqSchema.OUTBOX_ATTEMPTS,
                message.attempts,
            ).set(
                JooqSchema.OUTBOX_LAST_ERROR,
                message.lastError,
            ).execute()
    }

    override fun findPending(limit: Int): List<OutboxMessage> =
        dsl
            .select(
                JooqSchema.OUTBOX_ID,
                JooqSchema.OUTBOX_AGGREGATE_ID,
                JooqSchema.OUTBOX_EVENT_TYPE,
                JooqSchema.OUTBOX_PAYLOAD,
                JooqSchema.OUTBOX_CREATED_AT,
                JooqSchema.OUTBOX_PUBLISHED_AT,
                JooqSchema.OUTBOX_ATTEMPTS,
                JooqSchema.OUTBOX_LAST_ERROR,
            ).from(JooqSchema.OUTBOX_MESSAGES)
            .where(
                JooqSchema.OUTBOX_PUBLISHED_AT.isNull,
            ).orderBy(
                JooqSchema.OUTBOX_CREATED_AT.asc(),
            ).limit(limit)
            .fetch { record ->
                OutboxMessage(
                    id = record[JooqSchema.OUTBOX_ID]!!,
                    aggregateId =
                        record[JooqSchema.OUTBOX_AGGREGATE_ID]!!,
                    eventType =
                        record[JooqSchema.OUTBOX_EVENT_TYPE]!!,
                    payload =
                        record[JooqSchema.OUTBOX_PAYLOAD]!!,
                    createdAt =
                        record[JooqSchema.OUTBOX_CREATED_AT]!!
                            .toInstant(),
                    publishedAt =
                        record[JooqSchema.OUTBOX_PUBLISHED_AT]
                            ?.toInstant(),
                    attempts =
                        record[JooqSchema.OUTBOX_ATTEMPTS]!!,
                    lastError =
                        record[JooqSchema.OUTBOX_LAST_ERROR],
                )
            }

    override fun markPublished(id: UUID) {
        dsl
            .update(JooqSchema.OUTBOX_MESSAGES)
            .set(
                JooqSchema.OUTBOX_PUBLISHED_AT,
                Instant.now().toOffsetDateTime(),
            ).where(
                JooqSchema.OUTBOX_ID.eq(id),
            ).execute()
    }

    override fun markFailed(
        id: UUID,
        error: String,
    ) {
        dsl
            .update(JooqSchema.OUTBOX_MESSAGES)
            .set(
                JooqSchema.OUTBOX_ATTEMPTS,
                JooqSchema.OUTBOX_ATTEMPTS.plus(1),
            ).set(
                JooqSchema.OUTBOX_LAST_ERROR,
                error.take(4000),
            ).where(
                JooqSchema.OUTBOX_ID.eq(id),
            ).execute()
    }

    private fun Instant.toOffsetDateTime(): OffsetDateTime = atOffset(ZoneOffset.UTC)
}
