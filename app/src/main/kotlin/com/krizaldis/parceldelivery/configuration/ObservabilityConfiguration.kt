package com.krizaldis.parceldelivery.configuration

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObservabilityConfiguration {
    @Bean
    fun micrometerRegistry(): MeterRegistry = SimpleMeterRegistry()
}
