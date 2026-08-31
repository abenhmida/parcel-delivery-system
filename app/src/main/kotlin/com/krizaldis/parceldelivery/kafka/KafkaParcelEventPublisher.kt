package com.krizaldis.parceldelivery.kafka

import com.krizaldis.parceldelivery.application.events.ParcelEvent
import com.krizaldis.parceldelivery.application.events.ParcelEventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaParcelEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, ParcelEvent>,
    @param:Value("\${parcel.kafka.topic}") private val topic: String,
) : ParcelEventPublisher {
    override fun publish(event: ParcelEvent) {
        kafkaTemplate.send(topic, event.parcelId.toString(), event)
    }
}
