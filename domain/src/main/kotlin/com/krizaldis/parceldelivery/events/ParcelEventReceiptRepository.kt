package com.krizaldis.parceldelivery.events

import java.util.UUID

interface ParcelEventReceiptRepository {
    fun record(event: ParcelEvent): Boolean

    fun exists(eventId: UUID): Boolean
}
