package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JooqParcelRepositoryTest {
    private val postgres = PostgreSQLContainer("postgres:17")
    private lateinit var repository: JooqParcelRepository

    @BeforeAll
    fun start() {
        postgres.start()
        val dataSource =
            DatabaseFactory.createDataSource(
                DatabaseProperties(
                    url = postgres.jdbcUrl,
                    username = postgres.username,
                    password = postgres.password,
                ),
            )
        DatabaseFactory.migrate(dataSource)
        repository = JooqParcelRepository(DatabaseFactory.createDsl(dataSource))
    }

    @AfterAll
    fun stop() {
        postgres.stop()
    }

    @Test
    fun `create and reload preserves parcel and tracking history`() {
        val parcel =
            Parcel.create(
                trackingNumber = "PD-ROUNDTRIP",
                sender = address("Alice"),
                recipient = address("Bob"),
                weight = BigDecimal("2.500"),
                clock = fixedClock(),
            )

        parcel.pickUp(fixedClock())
        parcel.arriveAtSortingCenter(fixedClock())

        // Create a fresh parcel that represents the complete current aggregate.
        // The repository's create contract persists the current aggregate state
        // and its complete event history atomically.
        repository.create(parcel)

        val restored = repository.findById(parcel.id)

        assertThat(restored).isNotNull
        assertThat(restored!!.trackingNumber).isEqualTo(parcel.trackingNumber)
        assertThat(restored.status).isEqualTo(ParcelStatus.AT_SORTING_CENTER)
        assertThat(restored.trackingEvents.map { it.status })
            .containsExactly(
                ParcelStatus.CREATED,
                ParcelStatus.PICKED_UP,
                ParcelStatus.AT_SORTING_CENTER,
            )
    }

    @Test
    fun `update adds exactly one new tracking event`() {
        val parcel =
            Parcel.create(
                trackingNumber = "PD-UPDATE",
                sender = address("Alice"),
                recipient = address("Bob"),
                weight = BigDecimal("1.0"),
                clock = fixedClock(),
            )

        repository.create(parcel)

        parcel.pickUp(fixedClock())
        repository.update(parcel)

        val restored = repository.findById(parcel.id)

        assertThat(restored!!.status).isEqualTo(ParcelStatus.PICKED_UP)
        assertThat(restored.trackingEvents.map { it.status })
            .containsExactly(
                ParcelStatus.CREATED,
                ParcelStatus.PICKED_UP,
            )
    }

    @Test
    fun `lookup by tracking number returns parcel`() {
        val parcel =
            Parcel.create(
                trackingNumber = "PD-LOOKUP",
                sender = address("Alice"),
                recipient = address("Bob"),
                weight = BigDecimal("1.0"),
                clock = fixedClock(),
            )

        repository.create(parcel)

        assertThat(repository.findByTrackingNumber("PD-LOOKUP")!!.id)
            .isEqualTo(parcel.id)
    }

    private fun fixedClock() =
        Clock.fixed(
            Instant.parse("2026-08-30T10:00:00Z"),
            ZoneOffset.UTC,
        )

    private fun address(name: String) = Address(name, "1 Main Street", "Paris", "75001", "FR")
}
