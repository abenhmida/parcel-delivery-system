package com.krizaldis.parceldelivery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.krizaldis.parceldelivery"])
class ParcelDeliveryApplication

fun main(args: Array<String>) {
    runApplication<ParcelDeliveryApplication>(*args)
}
