package com.krizaldis.parceldelivery.domain

enum class ParcelStatus {
    CREATED,
    PICKED_UP,
    AT_SORTING_CENTER,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_FAILED,
    RETURNED,
}
