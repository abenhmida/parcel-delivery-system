package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.domain.Address
import com.krizaldis.parceldelivery.domain.Parcel
import com.krizaldis.parceldelivery.domain.ParcelRepository
import com.krizaldis.parceldelivery.domain.ParcelStatus
import com.krizaldis.parceldelivery.domain.TrackingEvent
import com.krizaldis.parceldelivery.infrastructure.database.jooq.tables.Parcels.PARCELS
import com.krizaldis.parceldelivery.infrastructure.database.jooq.tables.TrackingEvents.TRACKING_EVENTS
import com.krizaldis.parceldelivery.infrastructure.database.jooq.tables.records.ParcelsRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqParcelRepository(
    private val dsl: DSLContext,
) : ParcelRepository {
    override fun create(parcel: Parcel) {
        dsl.transaction { configuration ->
            val txDsl = configuration.dsl()

            txDsl
                .insertInto(PARCELS)
                .set(PARCELS.ID, parcel.id)
                .set(PARCELS.TRACKING_NUMBER, parcel.trackingNumber)
                .set(PARCELS.SENDER_NAME, parcel.sender.name)
                .set(PARCELS.SENDER_STREET, parcel.sender.street)
                .set(PARCELS.SENDER_CITY, parcel.sender.city)
                .set(PARCELS.SENDER_POSTAL_CODE, parcel.sender.postalCode)
                .set(PARCELS.SENDER_COUNTRY, parcel.sender.country)
                .set(PARCELS.RECIPIENT_NAME, parcel.recipient.name)
                .set(PARCELS.RECIPIENT_STREET, parcel.recipient.street)
                .set(PARCELS.RECIPIENT_CITY, parcel.recipient.city)
                .set(PARCELS.RECIPIENT_POSTAL_CODE, parcel.recipient.postalCode)
                .set(PARCELS.RECIPIENT_COUNTRY, parcel.recipient.country)
                .set(PARCELS.WEIGHT, parcel.weight)
                .set(PARCELS.STATUS, parcel.status.name)
                .set(PARCELS.CREATED_AT, OffsetDateTime.ofInstant(parcel.createdAt, ZoneId.systemDefault()))
                .onConflict(PARCELS.ID)
                .doUpdate()
                .set(PARCELS.STATUS, parcel.status.name)
                .execute()

            /*txDsl
                .deleteFrom(TRACKING_EVENTS)
                .where(TRACKING_EVENTS.PARCEL_ID.eq(parcel.id))
                .execute()*/

            parcel.trackingEvents.forEach { event ->
                txDsl
                    .insertInto(TRACKING_EVENTS)
                    .set(TRACKING_EVENTS.ID, UUID.randomUUID())
                    .set(TRACKING_EVENTS.PARCEL_ID, event.parcelId)
                    .set(TRACKING_EVENTS.STATUS, event.status.name)
                    .set(
                        TRACKING_EVENTS.OCCURRED_AT,
                        OffsetDateTime.ofInstant(event.occurredAt, ZoneId.systemDefault()),
                    ).execute()
            }
        }
    }

    override fun update(parcel: Parcel) {
        dsl.transaction { configuration ->
            val tx = configuration.dsl()

            val updated =
                tx
                    .update(JooqSchema.PARCELS)
                    .set(JooqSchema.STATUS, parcel.status.name)
                    .where(JooqSchema.PARCEL_ID.eq(parcel.id))
                    .execute()

            check(updated == 1) {
                "Parcel ${parcel.id} was not found during update"
            }

            insertEvent(tx, parcel.latestTrackingEvent())
        }
    }

    private fun insertEvent(
        tx: DSLContext,
        event: TrackingEvent,
    ) {
        tx
            .insertInto(JooqSchema.TRACKING_EVENTS)
            .set(JooqSchema.EVENT_ID, event.id)
            .set(JooqSchema.EVENT_PARCEL_ID, event.parcelId)
            .set(JooqSchema.EVENT_STATUS, event.status.name)
            .set(JooqSchema.EVENT_OCCURRED_AT, event.occurredAt.atOffset(ZoneOffset.UTC))
            .execute()
    }

    override fun findById(id: UUID): Parcel? =
        dsl
            .selectFrom(PARCELS)
            .where(PARCELS.ID.eq(id))
            .fetchOne()
            ?.let(::toDomain)

    override fun findByTrackingNumber(trackingNumber: String): Parcel? {
        val parcelRecord =
            dsl
                .selectFrom(PARCELS)
                .where(PARCELS.TRACKING_NUMBER.eq(trackingNumber))
                .fetchOne()
                ?: return null

        val events = findTrackingEvents(parcelRecord.id)

        return parcelRecord.toDomain(events)
    }

    private fun findTrackingEvents(parcelId: UUID): List<TrackingEvent> =
        dsl
            .selectFrom(TRACKING_EVENTS)
            .where(TRACKING_EVENTS.PARCEL_ID.eq(parcelId))
            .orderBy(TRACKING_EVENTS.OCCURRED_AT.asc())
            .fetch()
            .map {
                TrackingEvent(
                    parcelId = it.parcelId,
                    status = ParcelStatus.valueOf(it.status),
                    occurredAt = it.occurredAt.toInstant(),
                    id = it.id,
                )
            }

    private fun ParcelsRecord.toDomain(events: List<TrackingEvent>): Parcel =
        Parcel.restore(
            id = id,
            trackingNumber = trackingNumber,
            sender =
                Address(
                    name = senderName,
                    street = senderStreet,
                    city = senderCity,
                    postalCode = senderPostalCode,
                    country = senderCountry,
                ),
            recipient =
                Address(
                    name = recipientName,
                    street = recipientStreet,
                    city = recipientCity,
                    postalCode = recipientPostalCode,
                    country = recipientCountry,
                ),
            weight = weight,
            createdAt = createdAt.toInstant(),
            status = ParcelStatus.valueOf(status),
            trackingEvents = events,
        )

    private fun toDomain(record: Record): Parcel {
        val id = record.get(JooqSchema.PARCEL_ID)!!
        val events =
            dsl
                .selectFrom(JooqSchema.TRACKING_EVENTS)
                .where(JooqSchema.EVENT_PARCEL_ID.eq(id))
                .orderBy(JooqSchema.EVENT_OCCURRED_AT.asc())
                .fetch()
                .map {
                    TrackingEvent(
                        id = it.get(JooqSchema.EVENT_ID)!!,
                        parcelId = it.get(JooqSchema.EVENT_PARCEL_ID)!!,
                        status = ParcelStatus.valueOf(it.get(JooqSchema.EVENT_STATUS)!!),
                        occurredAt = it.get(JooqSchema.EVENT_OCCURRED_AT)!!.toInstant(),
                    )
                }

        return Parcel.restore(
            id = id,
            trackingNumber = record.get(JooqSchema.TRACKING_NUMBER)!!,
            sender =
                Address(
                    name = record.get(JooqSchema.SENDER_NAME)!!,
                    street = record.get(JooqSchema.SENDER_STREET)!!,
                    city = record.get(JooqSchema.SENDER_CITY)!!,
                    postalCode = record.get(JooqSchema.SENDER_POSTAL_CODE)!!,
                    country = record.get(JooqSchema.SENDER_COUNTRY)!!,
                ),
            recipient =
                Address(
                    name = record.get(JooqSchema.RECIPIENT_NAME)!!,
                    street = record.get(JooqSchema.RECIPIENT_STREET)!!,
                    city = record.get(JooqSchema.RECIPIENT_CITY)!!,
                    postalCode = record.get(JooqSchema.RECIPIENT_POSTAL_CODE)!!,
                    country = record.get(JooqSchema.RECIPIENT_COUNTRY)!!,
                ),
            weight = record.get(JooqSchema.WEIGHT)!!,
            createdAt = record.get(JooqSchema.CREATED_AT)!!.toInstant(),
            status = ParcelStatus.valueOf(record.get(JooqSchema.STATUS)!!),
            trackingEvents = events,
        )
    }
}
