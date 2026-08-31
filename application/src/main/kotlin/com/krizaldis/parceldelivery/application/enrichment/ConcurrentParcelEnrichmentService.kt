package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ConcurrentParcelEnrichmentService(
    private val addressVerifier: AddressVerifier,
    private val routeCalculator: RouteCalculator,
    private val deliveryEstimator: DeliveryEstimator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun enrich(parcel: Parcel): ParcelEnrichmentResult =
        coroutineScope {
            val address =
                async(dispatcher) {
                    addressVerifier.verify(parcel)
                }

            val route =
                async(dispatcher) {
                    routeCalculator.calculate(parcel)
                }

            val estimate =
                async(dispatcher) {
                    deliveryEstimator.estimate(parcel)
                }

            ParcelEnrichmentResult(
                parcelId = parcel.id,
                addressVerification = address.await(),
                route = route.await(),
                deliveryEstimate = estimate.await(),
            )
        }
}
