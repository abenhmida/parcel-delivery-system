package com.krizaldis.parceldelivery.configuration

import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.ParcelService
import com.krizaldis.parceldelivery.domain.RandomTrackingNumberGenerator
import com.krizaldis.parceldelivery.domain.TrackingNumberGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class ParcelDeliveryConfiguration {
    @Bean
    fun parcelService(
        parcelRepository: ParcelRepository,
        trackingNumberGenerator: TrackingNumberGenerator,
    ): ParcelService =
        ParcelService(
            parcelRepository = parcelRepository,
            trackingNumberGenerator = trackingNumberGenerator,
            clock = Clock.systemUTC(),
        )

    @Bean
    fun trackingNumberGenerator(): TrackingNumberGenerator = RandomTrackingNumberGenerator()
}
