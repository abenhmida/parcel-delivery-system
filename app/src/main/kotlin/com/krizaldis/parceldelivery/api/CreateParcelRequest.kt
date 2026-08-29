package com.krizaldis.parceldelivery.api

import java.math.BigDecimal

data class CreateParcelRequest(
    val sender: AddressDTO,
    val recipient: AddressDTO,
    val weight: BigDecimal,
)
