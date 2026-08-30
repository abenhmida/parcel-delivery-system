package com.krizaldis.parceldelivery.api

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AddressRequest(
    val name: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
) {
    fun toDomain() = Address(name, street, city, postalCode, country)
}

data class CreateParcelRequest(
    val sender: AddressRequest,
    val recipient: AddressRequest,
    val weight: BigDecimal,
)

data class AddressResponse(
    val name: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
) {
    companion object {
        fun from(address: Address) =
            AddressResponse(
                address.name,
                address.street,
                address.city,
                address.postalCode,
                address.country,
            )
    }
}

data class ParcelResponse(
    val id: UUID,
    val trackingNumber: String,
    val status: String,
    val sender: AddressResponse,
    val recipient: AddressResponse,
    val weight: BigDecimal,
    val createdAt: Instant,
) {
    companion object {
        fun from(parcel: Parcel) =
            ParcelResponse(
                id = parcel.id,
                trackingNumber = parcel.trackingNumber,
                status = parcel.status.name,
                sender = AddressResponse.from(parcel.sender),
                recipient = AddressResponse.from(parcel.recipient),
                weight = parcel.weight,
                createdAt = parcel.createdAt,
            )
    }
}

data class TrackingEventResponse(
    val id: UUID,
    val status: String,
    val occurredAt: Instant,
)

data class TrackingResponse(
    val parcelId: UUID,
    val trackingNumber: String,
    val events: List<TrackingEventResponse>,
)

data class ApiError(
    val code: String,
    val message: String,
)
