package com.krizaldis.parceldelivery.events

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

class ParcelEventProcessorTest {
    @Test
    fun `processor limits concurrency`() =
        runBlocking {
            val running = AtomicInteger(0)

            val maximum = AtomicInteger(0)

            val handler =
                object : ParcelEventHandler {
                    override suspend fun handle(event: ParcelEvent) {
                        val current = running.incrementAndGet()

                        maximum.updateAndGet {
                            maxOf(it, current)
                        }

                        delay(50.milliseconds)

                        running.decrementAndGet()
                    }
                }
            val processor =
                ParcelEventProcessor(
                    handler = handler,
                    maxConcurrency = 2,
                )

            processor.process(
                List(10) {
                    event()
                },
            )
        }

    @Test
    fun `one failed event does not cancel other events`() =
        runBlocking {
            val processed = AtomicInteger(0)

            val handler =
                object : ParcelEventHandler {
                    override suspend fun handle(event: ParcelEvent) {
                        if (event.trackingNumber == "FAIL") {
                            throw IllegalArgumentException("boom")
                        }
                        processed.incrementAndGet()
                    }
                }
            val processor = ParcelEventProcessor(handler = handler, maxConcurrency = 4)

            val result =
                runCatching {
                    processor.process(
                        listOf(
                            event("FAIL"),
                            event("OK-1"),
                            event("OK-2"),
                        ),
                    )
                }
        }

    private fun event(trackingNumber: String): ParcelEvent = event().copy(trackingNumber = trackingNumber)

    private fun event() =
        ParcelEvent(
            eventId = UUID.randomUUID(),
            parcelId = UUID.randomUUID(),
            trackingNumber = "PD-${UUID.randomUUID()}",
            type = ParcelEventType.PARCEL_CREATED,
            occurredAt = Instant.now(),
        )
}
