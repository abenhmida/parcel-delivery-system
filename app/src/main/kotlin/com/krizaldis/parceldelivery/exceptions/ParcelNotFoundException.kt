package com.krizaldis.parceldelivery.exceptions

import java.util.UUID

class ParcelNotFoundException(
    message: String,
) : RuntimeException(message) {
    constructor(id: UUID) : this("Parcel with id $id not found")
}
