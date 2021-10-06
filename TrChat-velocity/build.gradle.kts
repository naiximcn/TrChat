plugins {
    id("io.izzel.taboolib") version "1.30"
}

repositories {
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.0.0")
}

taboolib {
    description {
        contributors {
            name("Arasple")
            name("ItsFlicker")
        }
        desc("Advanced Minecraft Chat Control")
    }
    install("common")
    install("module-configuration")
    install("module-lang")
    install("module-metrics")
    install("module-porticus")
    install("platform-velocity")
    classifier = null
    version = "6.0.3-8"
}