package com.krizaldis.parceldelivery.application.events

interface ParcelEventPublisher {
    fun publish(event: ParcelEvent)
}
