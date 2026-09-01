package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.DefaultParcelEventHandler
import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventType
import com.krizaldis.parceldelivery.infrastructure.database.JooqParcelEventReceiptRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class CoroutineParcelEventConsumerTest {
    private val repository = mock<JooqParcelEventReceiptRepository>()
    private val handler = DefaultParcelEventHandler(repository)

    @Test
    fun `event is recorded after successful handling`() =
        runTest {
            val event = event()

            whenever { repository.exists(event.eventId) }.thenReturn(false)

            handler.handle(event)

            verify(repository)
                .record(event)
        }

    private fun event() =
        ParcelEvent(
            eventId = UUID.randomUUID(),
            parcelId = UUID.randomUUID(),
            trackingNumber = "PD-123",
            type = ParcelEventType.PARCEL_CREATED,
            occurredAt = Instant.now(),
        )
}
