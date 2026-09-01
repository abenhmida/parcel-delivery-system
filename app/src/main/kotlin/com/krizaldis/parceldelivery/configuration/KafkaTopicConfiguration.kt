package com.krizaldis.parceldelivery.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaTopicConfiguration {
    @Bean
    fun parcelEventsTopic(
        @Value("\${parcel.kafka.topic}")
        topic: String,
    ): NewTopic =
        NewTopic(
            topic,
            6,
            1,
        )

    @Bean
    fun parcelEventsDeadLetterTopic(
        @Value("\${parcel.kafka.dead-letter-topic}")
        topic: String,
    ): NewTopic =
        NewTopic(
            topic,
            6,
            1,
        )
}
