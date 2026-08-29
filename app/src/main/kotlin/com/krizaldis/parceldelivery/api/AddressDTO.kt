package com.krizaldis.parceldelivery.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.krizaldis.parceldelivery.domain.Address

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

    fun toDomain(): Address =
        Address(
            name = this.name,
            street = this.street,
            city = this.city,
            postalCode = this.postalCode,
            country = this.country,
        )
}
