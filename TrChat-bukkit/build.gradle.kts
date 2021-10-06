plugins {
    id("io.izzel.taboolib") version "1.30"
}

repositories {
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://repo.mikeprimm.com/")
    }
}

dependencies {
    compileOnly("us.dynmap:dynmap-api:2.5")
    compileOnly("me.clip:placeholderapi:2.10.9")
    compileOnly("ink.ptms.core:v11701:11701:mapped")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    compileOnly("ink.ptms.core:v11604:11604:all")
    compileOnly("ink.ptms:nms-all:1.0.0")
}

taboolib {
    description {
        contributors {
            name("Arasple")
            name("ItsFlicker")
        }
        dependencies {
            name("PlaceholderAPI").optional(true)
            name("Dynmap").optional(true)
            name("DiscordSRV").optional(true)
        }
        desc("Advanced Minecraft Chat Control")
    }
    install("common", "common-5")
    install("module-chat")
    install("module-configuration")
    install("module-database")
    install("module-kether")
    install("module-lang")
    install("module-metrics")
    install("module-nms", "module-nms-util")
    install("module-porticus")
    install("module-ui")
    install("platform-bukkit")
    install("expansion-command-helper")
    classifier = null
    version = "6.0.3-8"
}