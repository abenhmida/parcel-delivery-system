package com.krizaldis.parceldelivery.events

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class PersistingParcelEventHandlerTest {
    @Test
    fun `duplicate event is ignored`() {
        val repository = mock<ParcelEventReceiptRepository>()

        val event =
            ParcelEvent(
                eventId = UUID.randomUUID(),
                parcelId = UUID.randomUUID(),
                trackingNumber = "PD-123",
                type = ParcelEventType.PARCEL_DELIVERED,
                occurredAt = Instant.now(),
            )

        whenever { repository.record(event) }
            .thenReturn(false)

        val handler =
            PersistingParcelEventHandler(repository)

        handler.handle(event)

        verify(repository, times(1))
            .record(event)
    }
}
