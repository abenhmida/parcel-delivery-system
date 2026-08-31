package com.krizaldis.parceldelivery.events

interface ParcelEventPublisher {
    fun publish(event: ParcelEvent)
}
