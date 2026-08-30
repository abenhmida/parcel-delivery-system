package com.krizaldis.parceldelivery.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.util.UUID

class ParcelTest {
    private val clock = Clock.systemUTC()

    @Test
    fun `new parcel starts in CREATED state`() {
        val parcel =
            Parcel.create(
                sender =
                    aSenderAddress(),
                recipient =
                    aRecipientAddress(),
                weight = BigDecimal("2.5"),
                trackingNumber = UUID.randomUUID().toString(),
                clock = Clock.systemUTC(),
            )

        assertThat(parcel.status)
            .isEqualTo(ParcelStatus.CREATED)
    }

    @Test
    fun `created parcel can be picked up`() {
        val parcel = aParcel()

        parcel.pickUp(clock)

        assertThat(parcel.status)
            .isEqualTo(ParcelStatus.PICKED_UP)
    }

    @Test
    fun `created parcel cannot be delivered`() {
        val parcel = aParcel()

        assertThatThrownBy {
            parcel.deliver(clock)
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `parcel can move through the complete successful lifecycle`() {
        val parcel = aParcel()

        parcel.pickUp(clock)
        assertThat(parcel.status).isEqualTo(ParcelStatus.PICKED_UP)

        parcel.arriveAtSortingCenter(clock)
        assertThat(parcel.status).isEqualTo(ParcelStatus.AT_SORTING_CENTER)

        parcel.dispatch(clock)
        assertThat(parcel.status).isEqualTo(ParcelStatus.IN_TRANSIT)

        parcel.outForDelivery(clock)
        assertThat(parcel.status).isEqualTo(ParcelStatus.OUT_FOR_DELIVERY)

        parcel.deliver(clock)
        assertThat(parcel.status).isEqualTo(ParcelStatus.DELIVERED)
    }

    @Test
    fun `parcel cannot have zero weight`() {
        assertThatThrownBy {
            Parcel.create(
                sender = aSenderAddress(),
                recipient = aRecipientAddress(),
                weight = BigDecimal.ZERO,
                trackingNumber = UUID.randomUUID().toString(),
                clock = Clock.systemUTC(),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `parcel cannot have negative weight`() {
        assertThatThrownBy {
            Parcel.create(
                sender = aSenderAddress(),
                recipient = aRecipientAddress(),
                weight = BigDecimal("-1"),
                trackingNumber = UUID.randomUUID().toString(),
                clock = Clock.systemUTC(),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun aParcel(): Parcel =
        Parcel.create(
            sender =
                aSenderAddress(),
            recipient =
                aRecipientAddress(),
            weight = BigDecimal("2.5"),
            trackingNumber = UUID.randomUUID().toString(),
            clock = Clock.systemUTC(),
        )

    private fun aRecipientAddress(): Address =
        Address(
            name = "Bob",
            street = "2 Oak Street",
            city = "Lyon",
            postalCode = "69001",
            country = "FR",
        )

    private fun aSenderAddress(): Address =
        Address(
            name = "Alice",
            street = "1 Main Street",
            city = "Paris",
            postalCode = "75001",
            country = "FR",
        )
}
