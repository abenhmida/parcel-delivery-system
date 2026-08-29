package com.krizaldis.parceldelivery.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.TrackingEvent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ParcelResponse(
    val id: UUID,
    val sender: AddressDTO,
    val recipient: AddressDTO,
    val weight: BigDecimal,
    @field:JsonProperty("created_at") val createdAt: Instant,
    @field:JsonProperty("tracking_number") val trackingNumber: String,
    val status: String,
    @field:JsonProperty("tracking_events") val trackingEvents: List<TrackingEventDTO>,
) {
    data class TrackingEventDTO(
        @field:JsonProperty("parcel_id") val parcelId: UUID,
        val status: String,
        @field:JsonProperty("occurred_at") val occurredAt: Instant,
    ) {
        companion object {
            fun from(trackingEvents: List<TrackingEvent>): List<TrackingEventDTO> =
                trackingEvents.map {
                    TrackingEventDTO(
                        parcelId = it.parcelId,
                        status = it.status.name,
                        occurredAt = it.occurredAt,
                    )
                }
        }
    }

    companion object {
        fun from(parcel: Parcel): ParcelResponse =
            ParcelResponse(
                id = parcel.id,
                sender = AddressDTO.from(parcel.sender),
                recipient = AddressDTO.from(parcel.recipient),
                weight = parcel.weight,
                createdAt = parcel.createdAt,
                trackingNumber = parcel.trackingNumber,
                status = parcel.status.name,
                trackingEvents = TrackingEventDTO.from(parcel.trackingEvents),
            )
    }
}
