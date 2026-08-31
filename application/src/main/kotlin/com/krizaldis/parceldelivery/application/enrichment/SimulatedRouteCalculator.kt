package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel
import kotlinx.coroutines.delay
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SimulatedRouteCalculator(
    private val latency: Duration = Duration.ofMillis(500),
) : RouteCalculator {
    override suspend fun calculate(parcel: Parcel): Route {
        delay(latency.toMillis().milliseconds)

        return Route(
            distanceKm = 465.2,
            estimatedMinutes = 285,
        )
    }
}
