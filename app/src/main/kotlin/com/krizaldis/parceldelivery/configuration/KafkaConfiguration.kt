package com.krizaldis.parceldelivery.configuration

import com.fasterxml.jackson.databind.JsonDeserializer
import com.krizaldis.parceldelivery.events.NonRetryableParcelEventException
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import com.krizaldis.parceldelivery.infrastructure.kafka.JacksonParcelEventSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JacksonJsonSerializer
import org.springframework.util.backoff.FixedBackOff
import tools.jackson.databind.json.JsonMapper

@Configuration
class KafkaConfiguration(
    @param:Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
) {
    @Bean
    fun kafkaTemplate(parcelEventProducerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> =
        KafkaTemplate(parcelEventProducerFactory)

    @Bean
    fun parcelEventProducerFactory(): ProducerFactory<String, Any> {
        val properties =
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonJsonSerializer::class.java,
            )

        return DefaultKafkaProducerFactory(properties)
    }

    @Bean
    fun parcelEventKafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory)

    @Bean
    fun kafkaErrorHandler(kafkaTemplate: KafkaTemplate<String, Any>): CommonErrorHandler {
        val backOff = FixedBackOff(1_000L, 2L)

        val recovery =
            DeadLetterPublishingRecoverer(
                kafkaTemplate,
            )

        return DefaultErrorHandler(recovery, backOff).apply {
            addNotRetryableExceptions(
                NonRetryableParcelEventException::class.java,
            )
        }
    }

    @Bean
    fun defaultConsumerFactory() =
        DefaultKafkaConsumerFactory<String, String>(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to "parcel-delivery-events",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to org.apache.kafka.common.serialization.StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ),
        )

    @Bean
    fun parcelEventConsumerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        errorHandler: CommonErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()

        factory.setConsumerFactory(consumerFactory)
        factory.setConcurrency(3)

        factory.containerProperties.ackMode = ContainerProperties.AckMode.RECORD

        factory.setCommonErrorHandler(errorHandler)

        return factory
    }

    @Bean
    fun jacksonParcelEventSerializer(objectMapper: JsonMapper): ParcelEventSerializer = JacksonParcelEventSerializer(objectMapper)
}
