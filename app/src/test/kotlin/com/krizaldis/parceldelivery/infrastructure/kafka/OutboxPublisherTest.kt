package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.application.outbox.OutboxMessage
import com.krizaldis.parceldelivery.application.outbox.OutboxRepository
import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import com.krizaldis.parceldelivery.events.ParcelEventType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class OutboxPublisherTest {
    @Test
    fun `successful publication marks message as published`() {
        val repository = mock<OutboxRepository>()

        val serializer =
            mock<ParcelEventSerializer>()

        val kafka =
            mock<KafkaTemplate<String, ParcelEvent>>()

        val event =
            event()

        val message =
            message(event)

        whenever(repository.findPending(100))
            .thenReturn(listOf(message))

        whenever(serializer.deserialize(message.payload))
            .thenReturn(event)

        whenever(
            kafka.send(
                "parcel-events",
                event.parcelId.toString(),
                event,
            ),
        ).thenReturn(
            CompletableFuture.completedFuture(null),
        )

        val publisher =
            OutboxPublisher(
                outboxRepository = repository,
                serializer = serializer,
                kafkaTemplate = kafka,
                topic = "parcel-events",
            )

        publisher.publishPending()

        verify(repository)
            .markPublished(message.id)
    }

    @Test
    fun `failed publication marks message as failed`() {
        val repository =
            mock<OutboxRepository>()

        val serializer =
            mock<ParcelEventSerializer>()

        val kafka =
            mock<KafkaTemplate<String, ParcelEvent>>()

        val event =
            event()

        val message =
            message(event)

        whenever(repository.findPending(100))
            .thenReturn(listOf(message))

        whenever(serializer.deserialize(message.payload))
            .thenReturn(event)

        whenever(
            kafka.send(
                "parcel-events",
                event.parcelId.toString(),
                event,
            ),
        ).thenThrow(
            RuntimeException("Kafka unavailable"),
        )

        val publisher =
            OutboxPublisher(
                outboxRepository = repository,
                serializer = serializer,
                kafkaTemplate = kafka,
                topic = "parcel-events",
            )

        publisher.publishPending()

        verify(repository)
            .markFailed(
                message.id,
                "Kafka unavailable",
            )
    }

    private fun event() =
        ParcelEvent(
            eventId = UUID.randomUUID(),
            parcelId = UUID.randomUUID(),
            trackingNumber = "PD-123",
            type = ParcelEventType.PARCEL_CREATED,
            occurredAt = Instant.now(),
        )

    private fun message(event: ParcelEvent) =
        OutboxMessage(
            id = event.eventId,
            aggregateId = event.parcelId,
            eventType = event.type.name,
            payload = """{}""",
            createdAt = event.occurredAt,
            publishedAt = null,
            attempts = 0,
            lastError = null,
        )
}
