package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class KafkaParcelEventPublisherTest {
    @Test
    fun `publishes event using parcel id as Kafka key`() {
        val template = mock<KafkaTemplate<String, ParcelEvent>>()

        val event =
            ParcelEvent(
                eventId = UUID.randomUUID(),
                parcelId = UUID.randomUUID(),
                trackingNumber = "PD-123",
                type = ParcelEventType.PARCEL_PICKED_UP,
                occurredAt =
                    Instant.parse(
                        "2026-08-31T08:00:00Z",
                    ),
            )

        val future = CompletableFuture.completedFuture<SendResult<String, ParcelEvent>?>(null)

        whenever(
            template.send(
                "parcel-events",
                event.parcelId.toString(),
                event,
            ),
        ).thenReturn(future)

        val publisher =
            KafkaParcelEventPublisher(
                kafkaTemplate = template,
                topic = "parcel-events",
            )

        publisher.publish(event)

        verify(template).send(
            "parcel-events",
            event.parcelId.toString(),
            event,
        )
    }
}
