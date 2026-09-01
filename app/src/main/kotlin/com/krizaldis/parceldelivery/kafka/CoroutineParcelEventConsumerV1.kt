package com.krizaldis.parceldelivery.kafka

import com.krizaldis.parceldelivery.events.ParcelEventHandler
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class CoroutineParcelEventConsumerV1(
    private val serializer: ParcelEventSerializer,
    @param:Qualifier("defaultParcelEventHandler")
    private val handler: ParcelEventHandler,
) {
    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default,
        )

    @KafkaListener(
        topics = ["\${parcel.kafka.topic}"],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    fun consume(record: ConsumerRecord<String, String>) {
        val event = serializer.deserialize(record.value())

        scope.launch {
            try {
                handler.handle(event)

                logger.info {
                    "Processed event ${event.eventId}"
                }
            } catch (exception: Exception) {
                logger.error(exception) { "Failed processing event ${event.eventId}" }

                throw exception
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        scope.cancel()
    }
}
