package com.krizaldis.parceldelivery.events

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.TimeSource
import kotlin.time.toJavaDuration

@Component
class ObservedParcelEventHandler(
    private val delegate: DefaultParcelEventHandler,
    private val meterRegistry: MeterRegistry,
) : ParcelEventHandler {
    private val active = AtomicInteger()

    init {
        meterRegistry.gauge(
            "parcel.event.active",
            active,
        )
    }

    override suspend fun handle(event: ParcelEvent) {
        active.incrementAndGet()
        val started = TimeSource.Monotonic.markNow()

        try {
            delegate.handle(event)

            meterRegistry
                .counter(
                    "parcel.event.processed",
                    "event_type",
                    event.type.name,
                ).increment()
        } catch (exception: Exception) {
            meterRegistry
                .counter(
                    "parcel.event.failed",
                    "event_type",
                    event.type.name,
                ).increment()

            throw exception
        } finally {
            active.decrementAndGet()
            meterRegistry
                .timer("parcel.event.processing", "event_type", event.type.name)
                .record(started.elapsedNow().toJavaDuration())
        }
    }
}
