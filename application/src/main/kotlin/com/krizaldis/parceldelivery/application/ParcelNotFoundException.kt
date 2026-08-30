package com.example.parceldelivery.application

class ParcelNotFoundException(
    identifier: String,
) : RuntimeException("Parcel with id $identifier not found")
