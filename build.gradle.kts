plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "com.example.parceldelivery"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

val junitJupiter = libs.junit.jupiter
val junitPlatformLauncher = libs.junit.platform.launcher
val assertjCore = libs.assertj.core

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    dependencies {
        "testImplementation"(junitJupiter)
        "testImplementation"(assertjCore)
        "testRuntimeOnly"(junitPlatformLauncher)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}