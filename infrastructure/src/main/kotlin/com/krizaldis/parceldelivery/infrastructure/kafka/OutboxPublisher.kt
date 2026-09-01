package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.application.outbox.OutboxMessage
import com.krizaldis.parceldelivery.application.outbox.OutboxRepository
import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled

private val logger = KotlinLogging.logger {}

class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val serializer: ParcelEventSerializer,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @param:Value("\${parcel.kafka.topic}")
    private val topic: String,
) {
    @Scheduled(fixedDelayString = "\${parcel.outbox.poll-delay-ms:1000}")
    fun publishPending() {
        val messages = outboxRepository.findPending(100)

        messages.forEach { message ->
            publish(message)
        }
    }

    private fun publish(message: OutboxMessage) {
        try {
            val event = serializer.deserialize(message.payload)

            kafkaTemplate
                .send(topic, message.aggregateId.toString(), event)
                .get()

            outboxRepository.markPublished(message.id)

            logger.info {
                "Published outbox message id=${message.id} eventType=${message.eventType} aggregateId=${message.aggregateId}"
            }
        } catch (exception: Exception) {
            outboxRepository.markFailed(
                message.id,
                exception.message ?: exception.javaClass.simpleName,
            )

            logger.error(exception) {
                "Failed to publish outbox message id=${message.id}"
            }
        }
    }
}
