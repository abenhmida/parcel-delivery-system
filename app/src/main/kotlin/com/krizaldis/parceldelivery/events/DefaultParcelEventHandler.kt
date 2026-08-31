package com.krizaldis.parceldelivery.events

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class DefaultParcelEventHandler(
    private val receiptRepository: ParcelEventReceiptRepository,
) : ParcelEventHandler {
    override suspend fun handle(event: ParcelEvent) {
        withContext(Dispatchers.IO) {
            handleBlocking(event)
        }
    }

    private fun handleBlocking(event: ParcelEvent) {
        if (receiptRepository.exists(event.eventId)) {
            logger.info {
                "Ignoring duplicate event ${event.eventId}"
            }
            return
        }

        when (event.type) {
            ParcelEventType.PARCEL_CREATED -> handleCreated(event)
            ParcelEventType.PARCEL_PICKED_UP -> handlePickup(event)
            ParcelEventType.PARCEL_AT_SORTING_CENTER -> handleSortingCenter(event)
            ParcelEventType.PARCEL_DISPATCHED -> handleDispatched(event)
            ParcelEventType.PARCEL_OUT_FOR_DELIVERY -> handleOutForDelivery(event)
            ParcelEventType.PARCEL_DELIVERED -> handleDelivered(event)
            ParcelEventType.PARCEL_DELIVERY_FAILED -> handleDeliveryFailed(event)
            ParcelEventType.PARCEL_RETURNED -> handleReturned(event)
        }

        receiptRepository.record(event)
    }

    private fun handleReturned(event: ParcelEvent) {
        logger.info {
            "Handling parcel returned: ${event.parcelId}"
        }
    }

    private fun handleDeliveryFailed(event: ParcelEvent) {
        logger.info {
            "Handling parcel delivery failure: ${event.parcelId}"
        }
    }

    private fun handleDelivered(event: ParcelEvent) {
        logger.info {
            "Handling parcel delivered: ${event.parcelId}"
        }
    }

    private fun handleOutForDelivery(event: ParcelEvent) {
        logger.info {
            "Handling parcel out for delivery: ${event.parcelId}"
        }
    }

    private fun handleDispatched(event: ParcelEvent) {
        logger.info {
            "Handling parcel dispatched: ${event.parcelId}"
        }
    }

    private fun handleSortingCenter(event: ParcelEvent) {
        logger.info {
            "Handling parcel at sorting center: ${event.parcelId}"
        }
    }

    private fun handlePickup(event: ParcelEvent) {
        logger.info {
            "Handling parcel picked up: ${event.parcelId}"
        }
    }

    private fun handleCreated(event: ParcelEvent) {
        logger.info {
            "${"Handling parcel created: {}"} ${
                event.parcelId
            }"
        }
    }
}
