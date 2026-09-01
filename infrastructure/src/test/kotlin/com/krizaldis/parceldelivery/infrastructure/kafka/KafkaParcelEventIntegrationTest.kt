package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventType
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerializer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.to

@Testcontainers
class KafkaParcelEventIntegrationTest {
    companion object {
        @Container
        @JvmStatic
        val kafka = KafkaContainer("apache/kafka:4.0.0")
    }

    @Test
    fun `parcel event can be published and consumed`() {
        val topic = "parcel-events-test"

        val producerFactory =
            DefaultKafkaProducerFactory<String, ParcelEvent>(
                mapOf(
                    "bootstrap.servers" to kafka.bootstrapServers,
                    "key.serializer" to StringSerializer::class.java,
                    "value.serializer" to JacksonJsonSerializer::class.java,
                ),
            )

        val template =
            KafkaTemplate(producerFactory)

        val event =
            ParcelEvent(
                eventId = UUID.randomUUID(),
                parcelId = UUID.randomUUID(),
                trackingNumber = "PD-123",
                type = ParcelEventType.PARCEL_PICKED_UP,
                occurredAt = Instant.now(),
            )

        template
            .send(topic, event.parcelId.toString(), event)
            .get()

        val consumerFactory =
            DefaultKafkaConsumerFactory<String, ParcelEvent>(
                mapOf(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
                    ConsumerConfig.GROUP_ID_CONFIG to UUID.randomUUID().toString(),
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JacksonJsonDeserializer::class.java,
                    JacksonJsonDeserializer.TRUSTED_PACKAGES to "com.example.parceldelivery.application.events",
                    JacksonJsonDeserializer.VALUE_DEFAULT_TYPE to ParcelEvent::class.java.name,
                ),
            )

        consumerFactory.createConsumer().use { consumer ->
            consumer.subscribe(listOf(topic))

            val records =
                consumer.poll(Duration.ofSeconds(10))

            assertThat(records)
                .isNotEmpty

            assertThat(records.first().value())
                .isEqualTo(event)

            assertThat(records.first().key())
                .isEqualTo(event.parcelId.toString())
        }
    }
}
