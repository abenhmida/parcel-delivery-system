package com.krizaldis.parceldelivery.api

import com.krizaldis.parceldelivery.application.enrichment.ParcelEnrichmentApplicationService
import com.krizaldis.parceldelivery.application.enrichment.ParcelEnrichmentResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/parcels")
class ParcelEnrichmentController(
    private val service: ParcelEnrichmentApplicationService,
) {
    @GetMapping("/{id}/enrichment")
    suspend fun get(
        @PathVariable("id") id: UUID,
    ): ParcelEnrichmentResponse = ParcelEnrichmentResponse.from(service.enrich(id))
}

data class ParcelEnrichmentResponse(
    val parcelId: UUID,
    val addressVerified: Boolean,
    val normalizedRecipient: String,
    val route: RouteResponse,
    val deliveryEstimate: DeliveryEstimateResponse,
) {
    companion object {
        fun from(result: ParcelEnrichmentResult): ParcelEnrichmentResponse =
            ParcelEnrichmentResponse(
                parcelId = result.parcelId,
                addressVerified = result.addressVerification.valid,
                normalizedRecipient = result.addressVerification.normalizedRecipient,
                route =
                    RouteResponse(
                        distanceKm = result.route.distanceKm,
                        estimatedMinutes = result.route.estimatedMinutes,
                    ),
                deliveryEstimate =
                    DeliveryEstimateResponse(
                        date = result.deliveryEstimate.deliveryDate,
                    ),
            )
    }
}

data class RouteResponse(
    val distanceKm: Double,
    val estimatedMinutes: Int,
)

data class DeliveryEstimateResponse(
    val date: LocalDate,
)
