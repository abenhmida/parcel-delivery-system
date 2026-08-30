package com.krizaldis.parceldelivery.api

import com.krizaldis.parceldelivery.application.ParcelApplicationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/events")
class TrackingEventsController(
    private val parcelService: ParcelApplicationService,
) {
    @GetMapping("/{id}/tracking")
    fun tracking(
        @PathVariable id: UUID,
    ): TrackingResponse {
        val parcel = parcelService.get(id)

        return TrackingResponse(
            parcelId = parcel.id,
            trackingNumber = parcel.trackingNumber,
            events =
                parcel.trackingEvents.map {
                    TrackingEventResponse(
                        status = it.status.name,
                        occurredAt = it.occurredAt,
                        id = id,
                    )
                },
        )
    }
}
