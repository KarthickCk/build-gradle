plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kaml)
    implementation(libs.clikt)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "com.adyen.filters.MainKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
