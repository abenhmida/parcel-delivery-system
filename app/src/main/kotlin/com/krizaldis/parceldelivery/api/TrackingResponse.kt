package com.krizaldis.parceldelivery.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class TrackingResponse(
    @field:JsonProperty("parcel_id") val parcelId: UUID,
    @field:JsonProperty("tracking_number") val trackingNumber: String,
    @field:JsonProperty("events") val events: List<TrackingEventResponse>,
)
