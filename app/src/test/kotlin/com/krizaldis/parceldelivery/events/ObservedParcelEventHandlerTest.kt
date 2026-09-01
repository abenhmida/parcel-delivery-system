package com.krizaldis.parceldelivery.events

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class ObservedParcelEventHandlerTest {
    @Test
    fun `records success metrics when delegate succeeds`() =
        runTest {
            val delegate = mock<DefaultParcelEventHandler>()
            val meterRegistry = SimpleMeterRegistry()
            val handler = ObservedParcelEventHandler(delegate, meterRegistry)

            val event =
                ParcelEvent(
                    eventId = UUID.randomUUID(),
                    parcelId = UUID.randomUUID(),
                    trackingNumber = "PD-123",
                    type = ParcelEventType.PARCEL_CREATED,
                    occurredAt = Instant.now(),
                )

            handler.handle(event)

            verify(delegate).handle(event)

            val successCounter =
                meterRegistry
                    .find("parcel.event.processed")
                    .tag("event_type", ParcelEventType.PARCEL_CREATED.name)
                    .counter()
            assertEquals(1.0, successCounter?.count())

            val timer =
                meterRegistry
                    .find("parcel.event.processing")
                    .tag("event_type", ParcelEventType.PARCEL_CREATED.name)
                    .timer()
            assertEquals(1L, timer?.count())
            assertTrue((timer?.totalTime(java.util.concurrent.TimeUnit.NANOSECONDS) ?: 0.0) >= 0.0)
        }

    @Test
    fun `records failure metrics and timer when delegate throws`() =
        runTest {
            val delegate = mock<DefaultParcelEventHandler>()
            val meterRegistry = SimpleMeterRegistry()
            val handler = ObservedParcelEventHandler(delegate, meterRegistry)

            val event =
                ParcelEvent(
                    eventId = UUID.randomUUID(),
                    parcelId = UUID.randomUUID(),
                    trackingNumber = "PD-123",
                    type = ParcelEventType.PARCEL_DELIVERED,
                    occurredAt = Instant.now(),
                )

            whenever(delegate.handle(event)).thenAnswer {
                throw RuntimeException("Handler error")
            }

            assertThrows(RuntimeException::class.java) {
                kotlinx.coroutines.runBlocking {
                    handler.handle(event)
                }
            }

            val failedCounter =
                meterRegistry
                    .find("parcel.event.failed")
                    .tag("event_type", ParcelEventType.PARCEL_DELIVERED.name)
                    .counter()
            assertEquals(1.0, failedCounter?.count())

            val timer =
                meterRegistry
                    .find("parcel.event.processing")
                    .tag("event_type", ParcelEventType.PARCEL_DELIVERED.name)
                    .timer()
            assertEquals(1L, timer?.count())
        }
}
