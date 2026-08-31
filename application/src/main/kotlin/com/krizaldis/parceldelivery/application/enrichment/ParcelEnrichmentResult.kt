package com.krizaldis.parceldelivery.application.enrichment

import java.util.UUID

data class ParcelEnrichmentResult(
    val parcelId: UUID,
    val addressVerification: AddressVerification,
    val route: Route,
    val deliveryEstimate: DeliveryEstimate,
)
