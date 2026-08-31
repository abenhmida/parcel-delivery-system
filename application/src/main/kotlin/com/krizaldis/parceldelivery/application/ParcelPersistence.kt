package com.krizaldis.parceldelivery.application

import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.events.ParcelEvent

interface ParcelPersistence {
    fun create(
        parcel: Parcel,
        event: ParcelEvent,
    )

    fun update(
        parcel: Parcel,
        event: ParcelEvent,
    )
}
