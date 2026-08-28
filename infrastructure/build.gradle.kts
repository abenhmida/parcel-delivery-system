plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jooq.codegen)
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.jooq)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    runtimeOnly(libs.postgresql)
    jooqCodegen(libs.postgresql)
}
