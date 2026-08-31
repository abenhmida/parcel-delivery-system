package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.application.ParcelPersistence
import com.krizaldis.parceldelivery.application.outbox.OutboxMessage
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class TransactionalParcelPersistence(
    private val dsl: DSLContext,
    private val parcelRepository: JooqParcelRepository,
    private val eventSerializer: ParcelEventSerializer,
) : ParcelPersistence {
    override fun create(
        parcel: Parcel,
        event: ParcelEvent,
    ) {
        dsl.transactionResult { _ ->
            parcelRepository.create(parcel)
            insertOutbox(event)
        }
    }

    override fun update(
        parcel: Parcel,
        event: ParcelEvent,
    ) {
        dsl.transactionResult { _ ->
            parcelRepository.update(parcel)
            insertOutbox(event)
        }
    }

    private fun insertOutbox(event: ParcelEvent) {
        val message =
            OutboxMessage(
                id = event.eventId,
                aggregateId = event.parcelId,
                eventType = event.type.name,
                payload = eventPayload(event),
                createdAt = event.occurredAt,
                publishedAt = null,
                attempts = 0,
                lastError = null,
            )

        dsl
            .insertInto(JooqSchema.OUTBOX_MESSAGES)
            .set(JooqSchema.OUTBOX_ID, message.id)
            .set(
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
                message.createdAt.atOffset(
                    java.time.ZoneOffset.UTC,
                ),
            ).set(
                JooqSchema.OUTBOX_ATTEMPTS,
                message.attempts,
            ).execute()
    }

    private fun eventPayload(event: ParcelEvent): String =
        """
        {
          "eventId":"${event.eventId}",
          "parcelId":"${event.parcelId}",
          "trackingNumber":"${event.trackingNumber}",
          "type":"${event.type}",
          "occurredAt":"${event.occurredAt}"
        }
        """.trimIndent()
}
