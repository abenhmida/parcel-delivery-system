package com.krizaldis.parceldelivery.domain

import java.util.UUID

interface TrackingEventRepository {
    fun save(event: TrackingEvent)

    fun findByParcelId(parcelId: UUID): List<TrackingEvent>
}
