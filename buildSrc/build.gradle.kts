plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        url = uri("https://maven.wallentines.org/releases/")
    }
    maven {
        url = uri("https://maven.wallentines.org/plugins/")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.1")
    implementation("net.fabricmc:fabric-loom:1.16-SNAPSHOT")
    implementation("org.wallentines:gradle-pack-uploader:0.3.0")
}

