package com.krizaldis.parceldelivery.events

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class PersistingParcelEventHandler(
    private val receiptRepository: ParcelEventReceiptRepository,
) : ParcelEventHandler {
    override suspend fun handle(event: ParcelEvent) {
        val inserted = receiptRepository.record(event)

        if (inserted) {
            logger.info {
                "Processed parcel event " +
                    "eventId=${event.eventId} " +
                    "parcelId=${event.parcelId} " +
                    "type=${event.type}"
            }
        } else {
            logger.info {
                "Ignored duplicate parcel event eventId=${event.eventId}"
            }
        }
    }
}
