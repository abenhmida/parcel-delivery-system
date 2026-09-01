plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.jooq.codegen)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    implementation(libs.jooq)
    implementation(libs.jooq.kotlin)

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.context)
    implementation(libs.spring.boot.jooq)
    implementation(libs.jooq.kotlin)
    implementation(libs.spring.kafka)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    implementation(libs.kotlin.logging.jvm)

    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)

    // implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.module.kotlin)

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.logback.classic)

    jooqCodegen(libs.postgresql)
    jooqCodegen(libs.jooqMetaExtensions)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.postgresql)
    testImplementation(libs.mockito.kotlin)
}

jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/migration"
                    }
                    property {
                        key = "sort"
                        value = "flyway"
                    }
                    property {
                        key = "defaultNameCase"
                        value = "lower"
                    }
                }
            }
            target {
                packageName = "com.krizaldis.parceldelivery.infrastructure.database.jooq"
            }
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("jooqCodegen")
}
