package com.krizaldis.parceldelivery.domain

import java.math.BigDecimal
import java.time.Clock

class ParcelService(
    private val parcelRepository: ParcelRepository,
    private val trackingNumberGenerator: TrackingNumberGenerator,
    private val clock: Clock,
) {
    fun create(
        sender: Address,
        recipient: Address,
        weight: BigDecimal,
    ): Parcel {
        val parcel =
            Parcel.create(
                trackingNumber = trackingNumberGenerator.generate(),
                sender = sender,
                recipient = recipient,
                weight = weight,
                clock = clock,
            )

        parcelRepository.save(parcel)

        return parcel
    }
}
