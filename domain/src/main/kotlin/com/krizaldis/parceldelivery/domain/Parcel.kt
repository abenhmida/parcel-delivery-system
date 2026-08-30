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

    fun pickUp(clock: Clock) = transitionTo(ParcelStatus.PICKED_UP, clock)

    fun arriveAtSortingCenter(clock: Clock) = transitionTo(ParcelStatus.AT_SORTING_CENTER, clock)

    fun dispatch(clock: Clock) = transitionTo(ParcelStatus.IN_TRANSIT, clock)

    fun outForDelivery(clock: Clock) = transitionTo(ParcelStatus.OUT_FOR_DELIVERY, clock)

    fun deliver(clock: Clock) = transitionTo(ParcelStatus.DELIVERED, clock)

    fun deliveryFailed(clock: Clock) = transitionTo(ParcelStatus.DELIVERY_FAILED, clock)

    fun retryDelivery(clock: Clock) = transitionTo(ParcelStatus.OUT_FOR_DELIVERY, clock)

    fun returnToSender(clock: Clock) = transitionTo(ParcelStatus.RETURNED, clock)

    private fun transitionTo(
        newStatus: ParcelStatus,
        clock: Clock,
    ) {
        val allowed = allowedTransitions[currentStatus].orEmpty()

        check(newStatus in allowed) {
            "Invalid parcel transition: $currentStatus -> $newStatus"
        }
        currentStatus = newStatus
        events +=
            TrackingEvent(
                id = UUID.randomUUID(),
                parcelId = id,
                status = newStatus,
                occurredAt = Instant.now(clock),
            )
    }

    fun latestTrackingEvent(): TrackingEvent = events.last()

    companion object {
        fun create(
            sender: Address,
            recipient: Address,
            weight: BigDecimal,
            trackingNumber: String,
            clock: Clock,
        ): Parcel {
            require(trackingNumber.isNotBlank()) { "Tracking number must not be blank" }
            require(weight > BigDecimal.ZERO) { "Parcel weight must be greater than zero" }

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
                            id = UUID.randomUUID(),
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
        ): Parcel {
            require(trackingNumber.isNotBlank()) { "Tracking number must not be blank" }
            require(weight > BigDecimal.ZERO) { "Parcel weight must be greater than zero" }
            require(trackingEvents.all { it.parcelId == id }) {
                "All tracking events must belong to the parcel"
            }

            return Parcel(
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
}
