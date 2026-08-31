plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    application
}

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
    implementation(libs.spring.kafka)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)

    implementation(libs.jooq)

    implementation(libs.kotlin.reflect)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.rest.assured)
    testImplementation(libs.rest.assured.kotlin.extensions)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    runtimeOnly(libs.postgresql)
}
