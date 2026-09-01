package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventHandler
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import com.krizaldis.parceldelivery.events.ParcelEventType
import com.krizaldis.parceldelivery.kafka.CoroutineKafkaEventConsumer
import kotlinx.coroutines.delay
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class CoroutineKafkaEventConsumerTest {
    @Test
    fun `processing timeout propagates as listener failure`() {
        val serializer = mock<ParcelEventSerializer>()

        val handler =
            object : ParcelEventHandler {
                override suspend fun handle(event: ParcelEvent) {
                    delay(10.seconds)
                }
            }

        val event =
            ParcelEvent(
                eventId = UUID.randomUUID(),
                parcelId = UUID.randomUUID(),
                trackingNumber = "PD-TIMEOUT",
                type = ParcelEventType.PARCEL_CREATED,
                occurredAt = Instant.now(),
            )

        whenever { serializer.deserialize("payload") }.thenReturn(event)

        val sut =
            CoroutineKafkaEventConsumer(
                serializer = serializer,
                handler = handler,
                processingTimeoutMs = 50,
            )

        val record =
            ConsumerRecord(
                "parcel-events",
                0,
                0L,
                event.parcelId.toString(),
                "payload",
            )

        val result =
            runCatching {
                sut.consume(record)
            }

        assertThat(result.isFailure).isTrue()
    }
}
