plugins {
    id("io.izzel.taboolib") version "1.30"
}

dependencies {
    compileOnly("net.md-5:bungeecord-bootstrap:1.17-R0.1-SNAPSHOT")
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
    install("platform-bungee")
    classifier = null
    version = "6.0.3-8"
}