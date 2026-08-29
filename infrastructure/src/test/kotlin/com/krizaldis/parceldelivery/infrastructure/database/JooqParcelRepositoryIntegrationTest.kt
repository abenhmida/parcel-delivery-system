package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.ParcelStatus
import com.krizaldis.parceldelivery.domain.TrackingEvent
import com.krizaldis.parceldelivery.infrastructure.database.jooq.Tables
import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@Testcontainers
class JooqParcelRepositoryIntegrationTest {
    companion object {
        @Container
        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:17")

        private lateinit var dataSource: HikariDataSource
        private lateinit var dsl: DSLContext
        private lateinit var repository: ParcelRepository

        @JvmStatic
        @BeforeAll
        fun setUpAll() {
            postgres.start()

            dataSource =
                HikariDataSource().apply {
                    jdbcUrl = postgres.jdbcUrl
                    username = postgres.username
                    password = postgres.password
                    driverClassName = postgres.driverClassName
                }

            val flyway =
                Flyway
                    .configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load()
            flyway.migrate()

            dsl = DSL.using(dataSource, SQLDialect.POSTGRES)
            repository = JooqParcelRepository(dsl)
        }

        @JvmStatic
        @AfterAll
        fun tearDownAll() {
            if (::dataSource.isInitialized) {
                dataSource.close()
            }
        }
    }

    @BeforeEach
    fun cleanDatabase() {
        dsl.deleteFrom(Tables.TRACKING_EVENTS).execute()
        dsl.deleteFrom(Tables.PARCELS).execute()
    }

    @Test
    fun `should save and find parcel by id`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val sender = Address("Alice Sender", "Main St 1", "Berlin", "10115", "DE")
        val recipient = Address("Bob Receiver", "Second St 2", "Munich", "80331", "DE")
        val parcel =
            Parcel.create(
                sender = sender,
                recipient = recipient,
                weight = BigDecimal("3.750"),
                trackingNumber = "TRK-001",
                clock = clock,
            )

        repository.save(parcel)

        val found = repository.findById(parcel.id)

        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(parcel.id)
        assertThat(found?.trackingNumber).isEqualTo("TRK-001")
        assertThat(found?.sender).isEqualTo(sender)
        assertThat(found?.recipient).isEqualTo(recipient)
        assertThat(found?.weight).isEqualByComparingTo(BigDecimal("3.750"))
        assertThat(found?.createdAt).isEqualTo(now)
        assertThat(found?.status).isEqualTo(ParcelStatus.CREATED)
        assertThat(found?.trackingEvents).hasSize(1)
        assertThat(found?.trackingEvents?.first()?.parcelId).isEqualTo(parcel.id)
        assertThat(found?.trackingEvents?.first()?.status).isEqualTo(ParcelStatus.CREATED)
        assertThat(found?.trackingEvents?.first()?.occurredAt).isEqualTo(now)
    }

    @Test
    fun `should find parcel by tracking number`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val sender = Address("Alice", "Street A", "City A", "12345", "DE")
        val recipient = Address("Bob", "Street B", "City B", "67890", "DE")
        val parcel =
            Parcel.create(
                sender = sender,
                recipient = recipient,
                weight = BigDecimal("1.250"),
                trackingNumber = "TRK-TRACKING-99",
                clock = clock,
            )

        repository.save(parcel)

        val found = repository.findByTrackingNumber("TRK-TRACKING-99")
        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(parcel.id)
        assertThat(found?.trackingNumber).isEqualTo("TRK-TRACKING-99")
    }

    @Test
    fun `should return null when parcel not found by id`() {
        val found = repository.findById(UUID.randomUUID())
        assertThat(found).isNull()
    }

    @Test
    fun `should return null when parcel not found by tracking number`() {
        val found = repository.findByTrackingNumber("NON_EXISTING_TRACKING_NO")
        assertThat(found).isNull()
    }

    @Test
    fun `should save parcel with multiple tracking events and preserve chronological order`() {
        val baseTime = Instant.parse("2026-08-29T10:00:00Z")
        val parcelId = UUID.randomUUID()
        val event1 = TrackingEvent(parcelId, ParcelStatus.CREATED, baseTime)
        val event2 = TrackingEvent(parcelId, ParcelStatus.PICKED_UP, baseTime.plusSeconds(3600))
        val event3 = TrackingEvent(parcelId, ParcelStatus.AT_SORTING_CENTER, baseTime.plusSeconds(7200))

        val parcel =
            Parcel.restore(
                id = parcelId,
                trackingNumber = "TRK-MULTI-EVENTS",
                sender = Address("Sender Name", "Street 1", "City", "11111", "DE"),
                recipient = Address("Recipient Name", "Street 2", "City", "22222", "DE"),
                weight = BigDecimal("5.000"),
                createdAt = baseTime,
                status = ParcelStatus.AT_SORTING_CENTER,
                trackingEvents = listOf(event1, event2, event3),
            )

        repository.save(parcel)

        val found = repository.findById(parcelId)
        assertThat(found).isNotNull
        assertThat(found?.trackingEvents).hasSize(3)
        assertThat(found?.trackingEvents?.map { it.status }).containsExactly(
            ParcelStatus.CREATED,
            ParcelStatus.PICKED_UP,
            ParcelStatus.AT_SORTING_CENTER,
        )
        assertThat(found?.trackingEvents?.map { it.occurredAt }).containsExactly(
            baseTime,
            baseTime.plusSeconds(3600),
            baseTime.plusSeconds(7200),
        )
    }
}
