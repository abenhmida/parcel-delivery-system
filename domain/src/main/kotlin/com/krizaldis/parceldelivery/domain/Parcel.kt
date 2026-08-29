package com.krizaldis.parceldelivery.domain

import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.util.UUID

class Parcel private constructor(
    val id: UUID,
    val sender: Address,
    val recipient: Address,
    val weight: BigDecimal,
    val createdAt: Instant,
    val trackingNumber: String,
    initialStatus: ParcelStatus,
    initialTrackingEvents: List<TrackingEvent>,
) {
    private var currentStatus = initialStatus
    private val events = initialTrackingEvents.toMutableList()

    val status: ParcelStatus
        get() = currentStatus

    val trackingEvents: List<TrackingEvent>
        get() = events.toList()

    init {
        require(weight > BigDecimal.ZERO) {
            "Parcel weight must be greater than zero"
        }
    }

    private val allowedTransitions =
        mapOf(
            ParcelStatus.CREATED to
                setOf(
                    ParcelStatus.PICKED_UP,
                ),
            ParcelStatus.PICKED_UP to
                setOf(
                    ParcelStatus.AT_SORTING_CENTER,
                ),
            ParcelStatus.AT_SORTING_CENTER to
                setOf(
                    ParcelStatus.IN_TRANSIT,
                ),
            ParcelStatus.IN_TRANSIT to
                setOf(
                    ParcelStatus.OUT_FOR_DELIVERY,
                ),
            ParcelStatus.OUT_FOR_DELIVERY to
                setOf(
                    ParcelStatus.DELIVERED,
                    ParcelStatus.DELIVERY_FAILED,
                ),
            ParcelStatus.DELIVERY_FAILED to
                setOf(
                    ParcelStatus.OUT_FOR_DELIVERY,
                    ParcelStatus.RETURNED,
                ),
        )

    fun pickUp() = transitionTo(ParcelStatus.PICKED_UP)

    fun arriveAtSortingCenter() = transitionTo(ParcelStatus.AT_SORTING_CENTER)

    fun dispatch() = transitionTo(ParcelStatus.IN_TRANSIT)

    fun outForDelivery() = transitionTo(ParcelStatus.OUT_FOR_DELIVERY)

    fun deliver() = transitionTo(ParcelStatus.DELIVERED)

    fun deliveryFailed() = transitionTo(ParcelStatus.DELIVERY_FAILED)

    fun retryDelivery() = transitionTo(ParcelStatus.OUT_FOR_DELIVERY)

    fun returnToSender() = transitionTo(ParcelStatus.RETURNED)

    private fun transitionTo(newStatus: ParcelStatus) {
        check(newStatus in allowedTransitions.getValue(currentStatus)) {
            "Invalid parcel transition: $currentStatus -> $newStatus"
        }
        currentStatus = newStatus
    }

    companion object {
        fun create(
            sender: Address,
            recipient: Address,
            weight: BigDecimal,
            trackingNumber: String,
            clock: Clock,
        ): Parcel {
            val id = UUID.randomUUID()
            val now = Instant.now(clock)

            return Parcel(
                id = id,
                sender = sender,
                recipient = recipient,
                weight = weight,
                createdAt = now,
                initialStatus = ParcelStatus.CREATED,
                trackingNumber = trackingNumber,
                initialTrackingEvents =
                    listOf(
                        TrackingEvent(
                            parcelId = id,
                            status = ParcelStatus.CREATED,
                            occurredAt = now,
                        ),
                    ),
            )
        }

        fun restore(
            id: UUID,
            trackingNumber: String,
            sender: Address,
            recipient: Address,
            weight: BigDecimal,
            createdAt: Instant,
            status: ParcelStatus,
            trackingEvents: List<TrackingEvent>,
        ): Parcel =
            Parcel(
                id = id,
                trackingNumber = trackingNumber,
                sender = sender,
                recipient = recipient,
                weight = weight,
                createdAt = createdAt,
                initialStatus = status,
                initialTrackingEvents = trackingEvents,
            )
    }
}
