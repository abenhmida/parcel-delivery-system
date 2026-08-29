package com.krizaldis.parceldelivery.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelService
import com.krizaldis.parceldelivery.domain.TrackingEvent
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/parcels")
class ParcelController(
    private val parcelService: ParcelService,
) {
    @PostMapping
    fun create(
        @RequestBody request: CreateParcelRequest,
    ): ParcelResponse {
        val parcel =
            parcelService.create(
                sender = request.sender,
                recipient = request.recipient,
                weight = request.weight,
            )

        return ParcelResponse.from(parcel)
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
    ): ParcelResponse {
        val parcel =
            parcelService.get(id)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                )

        return ParcelResponse.from(parcel)
    }
}

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

data class CreateParcelRequest(
    val sender: AddressDTO,
    val recipient: AddressDTO,
    val weight: BigDecimal,
)

data class AddressDTO(
    val name: String,
    val street: String,
    val city: String,
    @field:JsonProperty("postal_code") val postalCode: String,
    val country: String,
) {
    companion object {
        fun from(sender: Address): AddressDTO =
            AddressDTO(
                name = sender.name,
                street = sender.street,
                city = sender.city,
                postalCode = sender.postalCode,
                country = sender.country,
            )
    }
}
