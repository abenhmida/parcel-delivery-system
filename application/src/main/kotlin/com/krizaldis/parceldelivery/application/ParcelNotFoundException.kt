package com.example.parceldelivery.application

class ParcelNotFoundException(identifier: String) :
    RuntimeException("Parcel not found: $identifier")
