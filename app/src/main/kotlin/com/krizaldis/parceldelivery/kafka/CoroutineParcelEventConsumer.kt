package com.krizaldis.parceldelivery.kafka

import com.krizaldis.parceldelivery.events.ParcelEventHandler
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class CoroutineParcelEventConsumer(
    private val serializer: ParcelEventSerializer,
    @param:Qualifier("defaultParcelEventHandler")
    private val handler: ParcelEventHandler,
) {
    @KafkaListener(
        topics = ["\${parcel.kafka.topic}"],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    fun consume(record: ConsumerRecord<String, String>) {
        val event = serializer.deserialize(record.value())

        runBlocking {
            handler.handle(event)
        }

        logger.info(
            "Processed event {}",
            event.eventId,
        )
    }
}
