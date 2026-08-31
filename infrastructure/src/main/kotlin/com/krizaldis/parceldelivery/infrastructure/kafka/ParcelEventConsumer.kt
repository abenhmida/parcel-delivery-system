package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventHandler
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener

class ParcelEventConsumer(
    private val handler: ParcelEventHandler,
) {
    @KafkaListener(
        topics = ["\${parcel.kafka.topic}"],
        groupId = "parcel-delivery-events",
        containerFactory = "parcelEventConsumerFactory",
        properties = ["spring.json.trusted.packages=com.krizaldis.parceldelivery.events"],
    )
    fun consume(event: ParcelEvent) {
        runBlocking {
            handler.handle(event)
        }
    }
}
