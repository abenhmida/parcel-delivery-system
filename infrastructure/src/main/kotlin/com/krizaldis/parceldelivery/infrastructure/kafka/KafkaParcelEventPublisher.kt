package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate

class KafkaParcelEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, ParcelEvent>,
    @param:Value("\${parcel.kafka.topic}") private val topic: String,
) : ParcelEventPublisher {
    override fun publish(event: ParcelEvent) {
        kafkaTemplate.send(topic, event.parcelId.toString(), event)
    }
}
