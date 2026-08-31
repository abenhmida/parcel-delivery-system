package com.krizaldis.parceldelivery.application.events

import com.krizaldis.parceldelivery.events.ParcelEvent

interface ParcelEventHandler {
    suspend fun handle(event: ParcelEvent)
}
