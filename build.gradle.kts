plugins {
    java
    id("io.izzel.taboolib") version "1.18"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

taboolib {
    description {
        contributors {
            name("Arasple")
        }
        dependencies {
            name("PlaceholderAPI").optional(true)
        }
        desc("Advanced Minecraft Chat Control")
    }
    install("common", "common-5")
    install("module-chat")
    install("module-configuration")
    install("module-database")
    install("module-lang")
    install("module-metrics")
    install("module-nms", "module-nms-util")
    install("module-ui")
    install("platform-bukkit", "platform-bungee")
    classifier = null
    version = "6.0.0-pre51"
}

repositories {
    mavenLocal()
    mavenCentral()
//    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
    maven{ url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven{ url = uri("https://repo.codemc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("ink.ptms.core:v11701:11701:mapped")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    compileOnly("ink.ptms.core:v11604:11604:all")
    compileOnly("net.md-5:bungeecord-bootstrap:1.17-R0.1-SNAPSHOT")
//    compileOnly("com.velocitypowered:velocity-api:1.1.8")
    compileOnly("me.clip:placeholderapi:2.10.9")
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