plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.logback.classic)
    testImplementation(libs.mockito.kotlin)
}
