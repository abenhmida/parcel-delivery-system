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
    jooqCodegen(libs.jooqMetaExtensions)
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
