package com.krizaldis.parceldelivery.events

interface ParcelEventHandler {
    fun handle(event: ParcelEvent)
}
