package com.krizaldis.parceldelivery.events

import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelStatus
import com.krizaldis.parceldelivery.domain.TrackingEvent

class ParcelEventFactory {
    fun create(
        parcel: Parcel,
        trackingEvent: TrackingEvent = parcel.latestTrackingEvent(),
    ): ParcelEvent =
        ParcelEvent(
            eventId = trackingEvent.id,
            parcelId = parcel.id,
            trackingNumber = parcel.trackingNumber,
            type = trackingEvent.status.toEventType(),
            occurredAt = trackingEvent.occurredAt,
        )

    private fun ParcelStatus.toEventType(): ParcelEventType =
        when (this) {
            ParcelStatus.CREATED -> {
                ParcelEventType.PARCEL_CREATED
            }

            ParcelStatus.PICKED_UP -> {
                ParcelEventType.PARCEL_PICKED_UP
            }

            ParcelStatus.AT_SORTING_CENTER -> {
                ParcelEventType.PARCEL_AT_SORTING_CENTER
            }

            ParcelStatus.IN_TRANSIT -> {
                ParcelEventType.PARCEL_DISPATCHED
            }

            ParcelStatus.OUT_FOR_DELIVERY -> {
                ParcelEventType.PARCEL_OUT_FOR_DELIVERY
            }

            ParcelStatus.DELIVERED -> {
                ParcelEventType.PARCEL_DELIVERED
            }

            ParcelStatus.DELIVERY_FAILED -> {
                ParcelEventType.PARCEL_DELIVERY_FAILED
            }

            ParcelStatus.RETURNED -> {
                ParcelEventType.PARCEL_RETURNED
            }
        }
}
