package com.krizaldis.parceldelivery.events

interface ParcelEventSerializer {
    fun serialize(event: ParcelEvent): String

    fun deserialize(payload: String): ParcelEvent
}
