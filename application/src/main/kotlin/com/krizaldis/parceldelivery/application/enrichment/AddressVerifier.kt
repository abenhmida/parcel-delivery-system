package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel

fun interface AddressVerifier {
    suspend fun verify(parcel: Parcel): AddressVerification
}

data class AddressVerification(
    val valid: Boolean,
    val normalizedRecipient: String,
)
