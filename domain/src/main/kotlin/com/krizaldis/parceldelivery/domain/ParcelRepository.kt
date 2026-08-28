package com.krizaldis.parceldelivery.domain

import java.util.UUID

interface ParcelRepository {
    fun save(parcel: Parcel)

    fun findById(id: UUID): Parcel?

    fun findByTrackingNumber(trackingNumber: String): Parcel?
}
