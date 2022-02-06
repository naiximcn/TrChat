plugins {
    java
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

taboolib {
    description {
        contributors {
            name("Arasple")
            name("ItsFlicker")
        }
        dependencies {
            name("PlaceholderAPI").with("bukkit")
            name("EcoEnchants").with("bukkit").loadafter(true)
        }
        desc("Advanced Minecraft Chat Control")
    }
    install("common", "common-5")
    install("module-chat")
    install("module-configuration")
    install("module-database", "module-database-mongodb")
    install("module-kether")
    install("module-lang")
    install("module-metrics")
    install("module-nms", "module-nms-util")
    install("module-porticus")
    install("module-ui")
    install("platform-bukkit", "platform-bungee", "platform-velocity")
    install("expansion-command-helper", "expansion-javascript")
    classifier = null
    version = "6.0.7-24"
}

repositories {
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.10.9")
    compileOnly("net.md-5:bungeecord-bootstrap:1.17-R0.1-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.0.0")
    compileOnly("ink.ptms.core:v11800:11800:api")
    compileOnly("ink.ptms.core:v11800:11800:universal")
    compileOnly("ink.ptms.core:v11800:11800:mapped")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}