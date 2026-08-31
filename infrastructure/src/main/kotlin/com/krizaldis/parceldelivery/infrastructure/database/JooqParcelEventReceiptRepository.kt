package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.events.ParcelEvent
import com.krizaldis.parceldelivery.events.ParcelEventReceiptRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqParcelEventReceiptRepository(
    private val dsl: DSLContext,
) : ParcelEventReceiptRepository {
    override fun record(event: ParcelEvent): Boolean {
        val inserted =
            dsl
                .insertInto(JooqSchema.PARCEL_EVENT_RECEIPTS)
                .set(JooqSchema.RECEIPT_EVENT_ID, event.eventId)
                .set(JooqSchema.RECEIPT_PARCEL_ID, event.parcelId)
                .set(JooqSchema.RECEIPT_TRACKING_NUMBER, event.trackingNumber)
                .set(JooqSchema.RECEIPT_EVENT_TYPE, event.type.name)
                .set(JooqSchema.RECEIPT_OCCURRED_AT, event.occurredAt.atOffset(ZoneOffset.UTC))
                .set(
                    JooqSchema.RECEIPT_RECEIVED_AT,
                    Instant.now().atOffset(ZoneOffset.UTC),
                ).onConflict(JooqSchema.RECEIPT_EVENT_ID)
                .doNothing()
                .execute()

        return inserted == 1
    }

    override fun exists(eventId: UUID): Boolean =
        dsl.fetchExists(
            dsl
                .selectOne()
                .from(JooqSchema.PARCEL_EVENT_RECEIPTS)
                .where(JooqSchema.RECEIPT_EVENT_ID.eq(eventId)),
        )
}
