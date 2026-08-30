package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.domain.ParcelStatus
import com.krizaldis.parceldelivery.domain.TrackingEvent
import com.krizaldis.parceldelivery.domain.TrackingEventRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqTrackingEventRepository(
    private val dsl: DSLContext,
) : TrackingEventRepository {
    override fun save(event: TrackingEvent) {
        dsl
            .insertInto(JooqSchema.TRACKING_EVENTS)
            .set(JooqSchema.EVENT_ID, event.id)
            .set(JooqSchema.EVENT_PARCEL_ID, event.parcelId)
            .set(JooqSchema.EVENT_STATUS, event.status.name)
            .set(
                JooqSchema.EVENT_OCCURRED_AT,
                OffsetDateTime.ofInstant(event.occurredAt, ZoneOffset.UTC),
            ).execute()
    }

    override fun findByParcelId(parcelId: UUID): List<TrackingEvent> =
        dsl
            .selectFrom(JooqSchema.TRACKING_EVENTS)
            .where(JooqSchema.EVENT_PARCEL_ID.eq(parcelId))
            .orderBy(JooqSchema.EVENT_OCCURRED_AT.asc())
            .fetch()
            .map { record ->
                TrackingEvent(
                    id = record.get(JooqSchema.EVENT_ID)!!,
                    parcelId = record.get(JooqSchema.EVENT_PARCEL_ID)!!,
                    status = ParcelStatus.valueOf(record.get(JooqSchema.EVENT_STATUS)!!),
                    occurredAt = record.get(JooqSchema.EVENT_OCCURRED_AT)!!.toInstant(),
                )
            }
}
