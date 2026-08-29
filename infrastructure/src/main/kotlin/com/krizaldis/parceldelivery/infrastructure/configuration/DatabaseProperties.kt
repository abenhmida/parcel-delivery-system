package com.krizaldis.parceldelivery.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.datasource")
data class DatabaseProperties(
    val url: String,
    val username: String,
    val password: String,
)
