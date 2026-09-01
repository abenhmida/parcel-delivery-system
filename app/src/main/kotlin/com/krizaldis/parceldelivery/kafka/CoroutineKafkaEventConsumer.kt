package com.krizaldis.parceldelivery.kafka

import com.krizaldis.parceldelivery.events.ParcelEventHandler
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.milliseconds

@Component
class CoroutineKafkaEventConsumer(
    private val serializer: ParcelEventSerializer,
    @param:Qualifier("persistingParcelEventHandler") private val handler: ParcelEventHandler,
    @param:Value("\${parcel.consumer.processing-timeout-ms:5000}")
    private val processingTimeoutMs: Long,
) {
    @KafkaListener(
        topics = ["\${parcel.kafka.topic}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consume(record: ConsumerRecord<String, String>) {
        val event = serializer.deserialize(record.value())

        runBlocking {
            withTimeout(processingTimeoutMs.milliseconds) {
                handler.handle(event)
            }
        }
    }
}
