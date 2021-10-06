plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        "compileOnly"(kotlin("stdlib"))
        "compileOnly"(fileTree("$rootDir/libs"))
    }
    tasks.withType<Jar> {
        destinationDirectory.set(file("$rootDir/build/libs"))
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}