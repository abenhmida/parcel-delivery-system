package com.krizaldis.parceldelivery.events

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ParcelEventFactoryTest {
    private val clock =
        Clock.fixed(
            Instant.parse("2026-08-31T08:00:00Z"),
            ZoneOffset.UTC,
        )

    @Test
    fun `created parcel produces PARCEL_CREATED event`() {
        val parcel =
            Parcel.create(
                trackingNumber = "PD-123456",
                sender = address("Alice"),
                recipient = address("Bob"),
                weight = BigDecimal("2.5"),
                clock = clock,
            )

        val event =
            ParcelEventFactory().create(parcel)

        assertThat(event.eventId)
            .isEqualTo(parcel.latestTrackingEvent().id)

        assertThat(event.parcelId)
            .isEqualTo(parcel.id)

        assertThat(event.trackingNumber)
            .isEqualTo("PD-123456")

        assertThat(event.type)
            .isEqualTo(ParcelEventType.PARCEL_CREATED)

        assertThat(event.occurredAt)
            .isEqualTo(Instant.parse("2026-08-31T08:00:00Z"))
    }

    @Test
    fun `pickup produces PARCEL_PICKED_UP event`() {
        val parcel =
            Parcel.create(
                trackingNumber = "PD-123456",
                sender = address("Alice"),
                recipient = address("Bob"),
                weight = BigDecimal("2.5"),
                clock = clock,
            )

        parcel.pickUp(clock)

        val event =
            ParcelEventFactory().create(parcel)

        assertThat(event.type)
            .isEqualTo(ParcelEventType.PARCEL_PICKED_UP)
    }

    private fun address(name: String) =
        Address(
            name = name,
            street = "1 Main Street",
            city = "Paris",
            postalCode = "75001",
            country = "FR",
        )
}
