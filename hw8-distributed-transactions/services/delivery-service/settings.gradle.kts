pluginManagement {
    val springBootVersion: String by settings
    plugins {
        id("org.springframework.boot") version springBootVersion
    }
}

rootProject.name = "delivery-service"
