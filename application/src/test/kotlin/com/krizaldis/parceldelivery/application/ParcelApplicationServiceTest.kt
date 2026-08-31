package com.krizaldis.parceldelivery.application

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.TrackingNumberGenerator
import com.krizaldis.parceldelivery.events.ParcelEventFactory
import com.krizaldis.parceldelivery.events.ParcelEventType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ParcelApplicationServiceTest {
    private val clock =
        Clock.fixed(
            Instant.parse("2026-08-30T10:00:00Z"),
            ZoneOffset.UTC,
        )
    private val repository = InMemoryParcelRepository()
    private val eventFactory = ParcelEventFactory()
    private val persistence = mock<ParcelPersistence>()

    private val service =
        ParcelApplicationService(
            parcelRepository = repository,
            trackingNumberGenerator =
                object : TrackingNumberGenerator {
                    override fun generate() = "PD-TEST123"
                },
            clock = clock,
            eventFactory = eventFactory,
            persistence = persistence,
        )

    @Test
    fun `create persists parcel`() {
        val parcel = service.create(address(), address(), BigDecimal("2.5"))

        verify(persistence).create(
            any(),
            check {
                assertThat(it.type)
                    .isEqualTo(
                        ParcelEventType.PARCEL_CREATED,
                    )
            },
        )

        assertThat(parcel.trackingNumber).isEqualTo("PD-TEST123")
    }

    @Test
    fun `transition updates persisted state`() {
        val parcel = service.create(address(), address(), BigDecimal("2.5"))

        repository.create(parcel)

        service.pickUp(parcel.id)

        verify(persistence).update(
            any(),
            check {
                assertThat(it.type)
                    .isEqualTo(
                        ParcelEventType.PARCEL_PICKED_UP,
                    )
            },
        )
    }

    private fun address() =
        Address(
            "Alice",
            "1 Main Street",
            "Paris",
            "75001",
            "FR",
        )
}

private class InMemoryParcelRepository : ParcelRepository {
    private val parcels = mutableMapOf<UUID, Parcel>()

    override fun create(parcel: Parcel) {
        parcels[parcel.id] = parcel
    }

    override fun update(parcel: Parcel) {
        parcels[parcel.id] = parcel
    }

    override fun findById(id: UUID): Parcel? = parcels[id]

    override fun findByTrackingNumber(trackingNumber: String): Parcel? = parcels.values.firstOrNull { it.trackingNumber == trackingNumber }
}
