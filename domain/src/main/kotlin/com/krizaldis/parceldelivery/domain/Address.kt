package com.krizaldis.parceldelivery.domain

data class Address(
    val name: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
) {
    companion object
}
