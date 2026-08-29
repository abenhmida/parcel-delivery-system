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
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@Repository
class JooqParcelRepository(
    private val dsl: DSLContext,
) : ParcelRepository {
    override fun save(parcel: Parcel) {
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
                .execute()

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

    override fun findById(id: UUID): Parcel? {
        val parcelRecord =
            dsl
                .selectFrom(PARCELS)
                .where(PARCELS.ID.eq(id))
                .fetchOne() ?: return null

        val events = findTrackingEvents(id)

        return parcelRecord.toDomain(events)
    }

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
}
