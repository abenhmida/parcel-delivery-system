package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel

fun interface RouteCalculator {
    suspend fun calculate(parcel: Parcel): Route
}

data class Route(
    val distanceKm: Double,
    val estimatedMinutes: Int,
)
