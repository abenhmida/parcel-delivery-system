package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

class SimulatedDeliveryEstimator(
    private val latency: Duration = Duration.ofMillis(200),
    private val clock: Clock = Clock.systemUTC(),
) : DeliveryEstimator {
    override suspend fun estimate(parcel: Parcel): DeliveryEstimate {
        delay(latency.toMillis().milliseconds)
        return DeliveryEstimate(LocalDate.now(clock).plusDays(2))
    }
}
