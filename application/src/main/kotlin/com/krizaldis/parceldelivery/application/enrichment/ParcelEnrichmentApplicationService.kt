package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.application.ParcelApplicationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ParcelEnrichmentApplicationService(
    private val parcelService: ParcelApplicationService,
    private val addressVerifier: AddressVerifier,
    private val routeCalculator: RouteCalculator,
    private val deliveryEstimator: DeliveryEstimator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val concurrentService: ConcurrentParcelEnrichmentService =
        ConcurrentParcelEnrichmentService(
            addressVerifier,
            routeCalculator,
            deliveryEstimator,
            dispatcher,
        ),
) {
    suspend fun enrich(id: UUID): ParcelEnrichmentResult {
        val parcel =
            withContext(dispatcher) {
                parcelService.get(id)
            }

        return concurrentService.enrich(parcel)
    }
}
