package com.krizaldis.parceldelivery.infrastructure.database

import com.krizaldis.parceldelivery.infrastructure.database.jooq.Tables.PARCELS
import com.krizaldis.parceldelivery.infrastructure.database.jooq.Tables.TRACKING_EVENTS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JooqGeneratedClassesTest {
    @Test
    fun `jooq generated tables are available and contain expected fields`() {
        assertThat(PARCELS).isNotNull
        assertThat(PARCELS.ID).isNotNull
        assertThat(PARCELS.TRACKING_NUMBER).isNotNull
        assertThat(PARCELS.SENDER_NAME).isNotNull
        assertThat(PARCELS.RECIPIENT_NAME).isNotNull
        assertThat(PARCELS.WEIGHT).isNotNull
        assertThat(PARCELS.STATUS).isNotNull
        assertThat(PARCELS.CREATED_AT).isNotNull

        assertThat(TRACKING_EVENTS).isNotNull
        assertThat(TRACKING_EVENTS.ID).isNotNull
        assertThat(TRACKING_EVENTS.PARCEL_ID).isNotNull
        assertThat(TRACKING_EVENTS.STATUS).isNotNull
        assertThat(TRACKING_EVENTS.OCCURRED_AT).isNotNull
    }
}
