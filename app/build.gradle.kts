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
    implementation(project(":infrastructure"))

    implementation(libs.spring.boot.starter)

    testImplementation(libs.spring.boot.starter.test)
}