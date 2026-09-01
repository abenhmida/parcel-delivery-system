package com.krizaldis.parceldelivery.infrastructure.kafka

import com.krizaldis.parceldelivery.events.NonRetryableParcelEventException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

class KafkaErrorHandlerTest {
    @Test
    fun `non retryable exception is classified`() {
        val handler =
            DefaultErrorHandler(
                FixedBackOff(1_000L, 2L),
            )

        handler.addNotRetryableExceptions(
            NonRetryableParcelEventException::class.java,
        )

        assertThat(
            handler.isAckAfterHandle,
        ).isTrue
    }
}
