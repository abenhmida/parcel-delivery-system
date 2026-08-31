package com.krizaldis.parceldelivery.application.enrichment

import com.krizaldis.parceldelivery.domain.Parcel
import kotlinx.coroutines.delay
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SimulatedAddressVerifier(
    private val latency: Duration = Duration.ofMillis(300),
) : AddressVerifier {
    override suspend fun verify(parcel: Parcel): AddressVerification {
        delay(latency.toMillis().milliseconds)

        return AddressVerification(
            valid = true,
            normalizedRecipient =
                listOf(
                    parcel.recipient.street,
                    parcel.recipient.postalCode,
                    parcel.recipient.city,
                    parcel.recipient.country,
                ).joinToString(", "),
        )
    }
}
