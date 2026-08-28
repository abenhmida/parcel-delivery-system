package com.krizaldis.parceldelivery.domain

import java.util.UUID

class RandomTrackingNumberGenerator : TrackingNumberGenerator {
    override fun generate(): String =
        "PD" +
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(22)
                .uppercase()
}
