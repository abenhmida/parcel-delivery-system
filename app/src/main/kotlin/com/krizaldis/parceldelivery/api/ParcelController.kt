package com.krizaldis.parceldelivery.api

import com.krizaldis.parceldelivery.application.ParcelApplicationService
import com.krizaldis.parceldelivery.domain.Address
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/parcels")
class ParcelController(
    private val parcelService: ParcelApplicationService,
) {
    @PostMapping
    fun create(
        @RequestBody request: CreateParcelRequest,
    ): ParcelResponse = ParcelResponse.from(parcelService.create(request))

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.get(id),
        )

    @GetMapping("/tracking/{trackingNumber}")
    fun getByTrackingNumber(
        @PathVariable trackingNumber: String,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.getByTrackingNumber(trackingNumber),
        )

    @PostMapping("/{id}/pickup")
    fun pickup(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.pickUp(id),
        )

    @PostMapping("/{id}/sorting")
    fun sorting(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.arriveAtSortingCenter(id),
        )

    @PostMapping("/{id}/dispatch")
    fun dispatch(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.dispatch(id),
        )

    @PostMapping("/{id}/out-for-delivery")
    fun outForDelivery(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.outForDelivery(id),
        )

    @PostMapping("/{id}/deliver")
    fun deliver(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.deliver(id),
        )

    @PostMapping("/{id}/delivery-failed")
    fun deliveryFailed(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.deliveryFailed(id),
        )

    @PostMapping("/{id}/retry-delivery")
    fun retryDelivery(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.retryDelivery(id),
        )

    @PostMapping("/{id}/return")
    fun returnToSender(
        @PathVariable id: UUID,
    ): ParcelResponse =
        ParcelResponse.from(
            parcelService.returnToSender(id),
        )
}
