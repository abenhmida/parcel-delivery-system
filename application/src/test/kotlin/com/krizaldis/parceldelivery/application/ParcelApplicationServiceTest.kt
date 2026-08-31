package com.krizaldis.parceldelivery.application

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.TrackingNumberGenerator
import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventFactory
import com.krizaldis.parceldelivery.events.ParcelEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
    private val eventPublisher =
        object : ParcelEventPublisher {
            override fun publish(event: ParcelEvent) {
            }
        }
    private val service =
        ParcelApplicationService(
            parcelRepository = repository,
            trackingNumberGenerator =
                object : TrackingNumberGenerator {
                    override fun generate() = "PD-TEST123"
                },
            clock = clock,
            eventFactory = eventFactory,
            eventPublisher = eventPublisher,
        )

    @Test
    fun `create persists parcel`() {
        val parcel = service.create(address(), address(), BigDecimal("2.5"))

        assertThat(repository.findById(parcel.id)).isNotNull
        assertThat(parcel.trackingNumber).isEqualTo("PD-TEST123")
    }

    @Test
    fun `transition updates persisted state`() {
        val parcel = service.create(address(), address(), BigDecimal("2.5"))

        service.pickUp(parcel.id)

        assertThat(repository.findById(parcel.id)!!.status.name)
            .isEqualTo("PICKED_UP")
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
