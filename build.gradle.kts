plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "com.krizaldis.parceldelivery"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

val junitJupiter = libs.junit.jupiter
val junitPlatformLauncher = libs.junit.platform.launcher
val assertjCore = libs.assertj.core

val junitEngine = libs.junit.platform.engine

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:6.1.3"))
        "testImplementation"(junitJupiter)
        "testImplementation"(assertjCore)
        "testRuntimeOnly"(junitPlatformLauncher)
        "testRuntimeOnly"(junitEngine)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        environment("DOCKER_API_VERSION", "1.44")
        systemProperty("docker.api.version", "1.44")
        systemProperty("api.version", "1.44")
    }
}
