package com.krizaldis.parceldelivery.infrastructure.database

import org.jooq.Field
import org.jooq.Table
import org.jooq.impl.DSL
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

object JooqSchema {
    val PARCELS: Table<*> = DSL.table(DSL.name("parcels"))
    val PARCEL_ID: Field<UUID> = DSL.field(DSL.name("id"), UUID::class.java)
    val TRACKING_NUMBER: Field<String> = DSL.field(DSL.name("tracking_number"), String::class.java)
    val SENDER_NAME: Field<String> = DSL.field(DSL.name("sender_name"), String::class.java)
    val SENDER_STREET: Field<String> = DSL.field(DSL.name("sender_street"), String::class.java)
    val SENDER_CITY: Field<String> = DSL.field(DSL.name("sender_city"), String::class.java)
    val SENDER_POSTAL_CODE: Field<String> = DSL.field(DSL.name("sender_postal_code"), String::class.java)
    val SENDER_COUNTRY: Field<String> = DSL.field(DSL.name("sender_country"), String::class.java)
    val RECIPIENT_NAME: Field<String> = DSL.field(DSL.name("recipient_name"), String::class.java)
    val RECIPIENT_STREET: Field<String> = DSL.field(DSL.name("recipient_street"), String::class.java)
    val RECIPIENT_CITY: Field<String> = DSL.field(DSL.name("recipient_city"), String::class.java)
    val RECIPIENT_POSTAL_CODE: Field<String> = DSL.field(DSL.name("recipient_postal_code"), String::class.java)
    val RECIPIENT_COUNTRY: Field<String> = DSL.field(DSL.name("recipient_country"), String::class.java)
    val WEIGHT: Field<BigDecimal> = DSL.field(DSL.name("weight"), BigDecimal::class.java)
    val STATUS: Field<String> = DSL.field(DSL.name("status"), String::class.java)
    val CREATED_AT: Field<OffsetDateTime> = DSL.field(DSL.name("created_at"), OffsetDateTime::class.java)

    val TRACKING_EVENTS: Table<*> = DSL.table(DSL.name("tracking_events"))
    val EVENT_ID: Field<UUID> = DSL.field(DSL.name("id"), UUID::class.java)
    val EVENT_PARCEL_ID: Field<UUID> = DSL.field(DSL.name("parcel_id"), UUID::class.java)
    val EVENT_STATUS: Field<String> = DSL.field(DSL.name("status"), String::class.java)
    val EVENT_OCCURRED_AT: Field<OffsetDateTime> =
        DSL.field(DSL.name("occurred_at"), OffsetDateTime::class.java)

    val PARCEL_EVENT_RECEIPTS =
        DSL.table(DSL.name("parcel_event_receipts"))

    val RECEIPT_EVENT_ID =
        DSL.field(
            DSL.name("event_id"),
            UUID::class.java,
        )

    val RECEIPT_PARCEL_ID =
        DSL.field(
            DSL.name("parcel_id"),
            UUID::class.java,
        )

    val RECEIPT_TRACKING_NUMBER =
        DSL.field(
            DSL.name("tracking_number"),
            String::class.java,
        )

    val RECEIPT_EVENT_TYPE =
        DSL.field(
            DSL.name("event_type"),
            String::class.java,
        )

    val RECEIPT_OCCURRED_AT =
        DSL.field(
            DSL.name("occurred_at"),
            OffsetDateTime::class.java,
        )

    val RECEIPT_RECEIVED_AT =
        DSL.field(
            DSL.name("received_at"),
            OffsetDateTime::class.java,
        )
}
