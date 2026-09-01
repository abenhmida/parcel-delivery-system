package com.krizaldis.parceldelivery.configuration

import com.krizaldis.parceldelivery.application.ParcelApplicationService
import com.krizaldis.parceldelivery.application.ParcelPersistence
import com.krizaldis.parceldelivery.application.enrichment.AddressVerifier
import com.krizaldis.parceldelivery.application.enrichment.DeliveryEstimator
import com.krizaldis.parceldelivery.application.enrichment.ParcelEnrichmentApplicationService
import com.krizaldis.parceldelivery.application.enrichment.RouteCalculator
import com.krizaldis.parceldelivery.application.enrichment.SimulatedAddressVerifier
import com.krizaldis.parceldelivery.application.enrichment.SimulatedDeliveryEstimator
import com.krizaldis.parceldelivery.application.enrichment.SimulatedRouteCalculator
import com.krizaldis.parceldelivery.application.outbox.OutboxRepository
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.RandomTrackingNumberGenerator
import com.krizaldis.parceldelivery.domain.TrackingNumberGenerator
import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventFactory
import com.krizaldis.parceldelivery.events.ParcelEventHandler
import com.krizaldis.parceldelivery.events.ParcelEventProcessor
import com.krizaldis.parceldelivery.events.ParcelEventReceiptRepository
import com.krizaldis.parceldelivery.events.ParcelEventSerializer
import com.krizaldis.parceldelivery.events.PersistingParcelEventHandler
import com.krizaldis.parceldelivery.infrastructure.database.DatabaseFactory
import com.krizaldis.parceldelivery.infrastructure.database.DatabaseProperties
import com.krizaldis.parceldelivery.infrastructure.kafka.OutboxPublisher
import com.krizaldis.parceldelivery.infrastructure.kafka.ParcelEventConsumer
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import java.time.Clock
import javax.sql.DataSource

@Configuration
class ParcelDeliveryConfiguration(
    @param:Value("\${spring.datasource.url}") private val url: String,
    @param:Value("\${spring.datasource.username}") private val username: String,
    @param:Value("\${spring.datasource.password}") private val password: String,
    @param:Value("\${parcel.kafka.topic}") private val topic: String,
) {
    @Bean
    fun databaseProperties() = DatabaseProperties(url, username, password)

    @Bean
    fun dataSource(properties: DatabaseProperties): DataSource = DatabaseFactory.createDataSource(properties)

    @Bean
    fun flyway(dataSource: DataSource): Flyway = DatabaseFactory.migrate(dataSource)

    @Bean
    fun dslContext(dataSource: DataSource): DSLContext = DatabaseFactory.createDsl(dataSource)

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun parcelService(
        parcelRepository: ParcelRepository,
        trackingNumberGenerator: TrackingNumberGenerator,
        clock: Clock,
        eventFactory: ParcelEventFactory,
        persistence: ParcelPersistence,
    ): ParcelApplicationService =
        ParcelApplicationService(
            parcelRepository = parcelRepository,
            trackingNumberGenerator = trackingNumberGenerator,
            clock = clock,
            eventFactory = eventFactory,
            persistence = persistence,
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

    @Bean
    fun outboxPublisher(
        outboxRepository: OutboxRepository,
        serializer: ParcelEventSerializer,
        kafkaTemplate: KafkaTemplate<String, Any>,
    ): OutboxPublisher =
        OutboxPublisher(
            outboxRepository = outboxRepository,
            serializer = serializer,
            kafkaTemplate = kafkaTemplate,
            topic = topic,
        )

    @Bean
    fun parcelEventFactory(): ParcelEventFactory = ParcelEventFactory()

    @Bean
    fun persistingParcelEventHandler(receiptRepository: ParcelEventReceiptRepository): ParcelEventHandler =
        PersistingParcelEventHandler(receiptRepository)

    @Bean
    fun parcelEventConsumer(
        @Qualifier("persistingParcelEventHandler") parcelEventHandler: ParcelEventHandler,
    ): ParcelEventConsumer = ParcelEventConsumer(parcelEventHandler)
}
