package com.krizaldis.parceldelivery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication(scanBasePackages = ["com.krizaldis.parceldelivery"])
class ParcelDeliveryApplication

fun main(args: Array<String>) {
    runApplication<ParcelDeliveryApplication>(*args)
}
