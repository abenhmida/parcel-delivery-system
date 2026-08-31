plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    application
}

extra["groovy.version"] = "4.0.24"

application {
    mainClass = "com.krizaldis.parceldelivery.ParcelDeliveryApplicationKt"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.flyway)
    compileOnly(libs.spring.boot.configuration.processor)
    implementation(libs.spring.kafka)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.spring.kafka)

    implementation(libs.jooq)

    implementation(libs.kotlin.logging.jvm)

    implementation(libs.kotlin.reflect)

    // implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)

    testImplementation(libs.rest.assured)
    testImplementation(libs.rest.assured.kotlin.extensions)

    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)

    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.mockito.kotlin)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.module.kotlin)

    runtimeOnly(libs.postgresql)
}
