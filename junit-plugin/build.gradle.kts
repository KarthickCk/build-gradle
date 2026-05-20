plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    plugins {
        create("xmlFilename") {
            id = "com.adyen.junit-plugin"
            implementationClass = "com.adyen.junitplugin.JUnitFilenamePlugin"
        }
    }
}