package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel
import java.time.LocalDate

fun interface DeliveryEstimator {
    suspend fun estimate(parcel: Parcel): DeliveryEstimate
}

data class DeliveryEstimate(
    val deliveryDate: LocalDate,
)
