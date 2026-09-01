package com.krizaldis.parceldelivery.infrastructure.kafka

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@EmbeddedKafka(
    partitions = 3,
    topics = ["parcel-events", "parcel-events.DLT"],
)
class ParcelKafkaIntegrationTest {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun kafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") {
                System.getProperty(
                    "spring.embedded.kafka.brokers",
                )
            }
        }
    }

    @Test
    fun `application context starts with kafka infrastructure`() {
        assertThat(true).isTrue()
    }
}
