package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel

class SequentialParcelEnrichmentService(
    private val addressVerifier: AddressVerifier,
    private val routeCalculator: RouteCalculator,
    private val deliveryEstimator: DeliveryEstimator,
) {
    suspend fun enrich(parcel: Parcel): ParcelEnrichmentResult {
        val address = addressVerifier.verify(parcel)
        val route = routeCalculator.calculate(parcel)
        val estimate = deliveryEstimator.estimate(parcel)

        return ParcelEnrichmentResult(
            parcelId = parcel.id,
            addressVerification = address,
            route = route,
            deliveryEstimate = estimate,
        )
    }
}
