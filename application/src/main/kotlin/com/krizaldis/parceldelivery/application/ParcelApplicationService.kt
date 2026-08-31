package com.krizaldis.parceldelivery.application

import com.example.parceldelivery.application.ParcelNotFoundException
import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.TrackingNumberGenerator
import com.krizaldis.parceldelivery.events.ParcelEventFactory
import com.krizaldis.parceldelivery.events.ParcelEventPublisher
import java.math.BigDecimal
import java.time.Clock
import java.util.UUID

class ParcelApplicationService(
    private val persistence: ParcelPersistence,
    private val parcelRepository: ParcelRepository,
    private val trackingNumberGenerator: TrackingNumberGenerator,
    private val clock: Clock,
    private val eventFactory: ParcelEventFactory,
) {
    fun create(
        sender: Address,
        recipient: Address,
        weight: BigDecimal,
    ): Parcel {
        val parcel =
            Parcel.create(
                trackingNumber = trackingNumberGenerator.generate(),
                sender = sender,
                recipient = recipient,
                weight = weight,
                clock = clock,
            )

        val event =
            eventFactory.create(parcel)

        persistence.create(
            parcel = parcel,
            event = event,
        )

        return parcel
    }

    fun get(id: UUID): Parcel =
        parcelRepository.findById(id)
            ?: throw ParcelNotFoundException(id.toString())

    fun getByTrackingNumber(trackingNumber: String): Parcel =
        parcelRepository.findByTrackingNumber(trackingNumber)
            ?: throw ParcelNotFoundException(trackingNumber)

    fun pickUp(id: UUID): Parcel =
        transition(id) {
            pickUp(clock)
        }

    fun arriveAtSortingCenter(id: UUID): Parcel =
        transition(id) {
            arriveAtSortingCenter(clock)
        }

    fun dispatch(id: UUID): Parcel =
        transition(id) {
            dispatch(clock)
        }

    fun outForDelivery(id: UUID): Parcel =
        transition(id) {
            outForDelivery(clock)
        }

    fun deliver(id: UUID): Parcel =
        transition(id) {
            deliver(clock)
        }

    fun deliveryFailed(id: UUID): Parcel =
        transition(id) {
            deliveryFailed(clock)
        }

    fun retryDelivery(id: UUID): Parcel =
        transition(id) {
            retryDelivery(clock)
        }

    fun returnToSender(id: UUID): Parcel =
        transition(id) {
            returnToSender(clock)
        }

    private fun transition(
        id: UUID,
        operation: Parcel.() -> Unit,
    ): Parcel {
        val parcel = get(id)
        val eventCountBefore = parcel.trackingEvents.size

        parcel.operation()

        check(parcel.trackingEvents.size == eventCountBefore + 1) {
            "A successful parcel transition must create exactly one tracking event"
        }

        val event =
            eventFactory.create(parcel)

        persistence.update(
            parcel = parcel,
            event = event,
        )

        return parcel
    }
}
