package com.krizaldis.parceldelivery.events

interface ParcelEventHandler {
    suspend fun handle(event: ParcelEvent)
}
