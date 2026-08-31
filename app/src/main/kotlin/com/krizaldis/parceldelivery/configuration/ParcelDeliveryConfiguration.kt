package com.krizaldis.parceldelivery.configuration

import com.krizaldis.parceldelivery.application.ParcelApplicationService
import com.krizaldis.parceldelivery.application.enrichment.AddressVerifier
import com.krizaldis.parceldelivery.application.enrichment.DeliveryEstimator
import com.krizaldis.parceldelivery.application.enrichment.ParcelEnrichmentApplicationService
import com.krizaldis.parceldelivery.application.enrichment.RouteCalculator
import com.krizaldis.parceldelivery.application.enrichment.SimulatedAddressVerifier
import com.krizaldis.parceldelivery.application.enrichment.SimulatedDeliveryEstimator
import com.krizaldis.parceldelivery.application.enrichment.SimulatedRouteCalculator
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.RandomTrackingNumberGenerator
import com.krizaldis.parceldelivery.domain.TrackingNumberGenerator
import com.krizaldis.parceldelivery.infrastructure.database.DatabaseFactory
import com.krizaldis.parceldelivery.infrastructure.database.DatabaseProperties
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.time.Clock
import javax.sql.DataSource

@Configuration
class ParcelDeliveryConfiguration(
    @param:Value("\${spring.datasource.url}") private val url: String,
    @param:Value("\${spring.datasource.username}") private val username: String,
    @param:Value("\${spring.datasource.password}") private val password: String,
) {
    @Autowired
    lateinit var env: Environment

    @Bean
    fun databaseProperties() = DatabaseProperties(url, username, password)

    @Bean(destroyMethod = "close")
    fun dataSource(properties: DatabaseProperties): DataSource {
        val dataSource = DatabaseFactory.createDataSource(properties)
        DatabaseFactory.migrate(dataSource)
        return dataSource
    }

    @Bean
    fun dslContext(dataSource: DataSource): DSLContext = DatabaseFactory.createDsl(dataSource)

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun parcelService(
        parcelRepository: ParcelRepository,
        trackingNumberGenerator: TrackingNumberGenerator,
        clock: Clock,
    ): ParcelApplicationService =
        ParcelApplicationService(
            parcelRepository = parcelRepository,
            trackingNumberGenerator = trackingNumberGenerator,
            clock = clock,
        )

    @Bean
    fun trackingNumberGenerator(): TrackingNumberGenerator = RandomTrackingNumberGenerator()

    @Bean
    fun addressVerifier(): AddressVerifier = SimulatedAddressVerifier()

    @Bean
    fun routeCalculator(): RouteCalculator = SimulatedRouteCalculator()

    @Bean
    fun deliveryEstimator(clock: Clock): DeliveryEstimator = SimulatedDeliveryEstimator(clock = clock)

    @Bean
    fun parcelEnrichmentApplicationService(
        parcelApplicationService: ParcelApplicationService,
        addressVerifier: AddressVerifier,
        routeCalculator: RouteCalculator,
        deliveryEstimator: DeliveryEstimator,
    ) = ParcelEnrichmentApplicationService(
        parcelService = parcelApplicationService,
        addressVerifier = addressVerifier,
        routeCalculator = routeCalculator,
        deliveryEstimator = deliveryEstimator,
    )
}
