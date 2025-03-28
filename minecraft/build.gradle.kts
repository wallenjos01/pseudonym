import buildlogic.Utils

plugins {
    id("build.fabric")
    id("build.publish")
}

Utils.setupResources(project, rootProject, "fabric.mod.json")

dependencies {

    minecraft("com.mojang:minecraft:${project.properties["minecraft-version"]}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric-loader-version"]}")

    // Fabric API
    listOf(
        "fabric-api-base",
        "fabric-resource-loader-v0"
    ).forEach { mod ->
        modApi(include(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))!!)
    }
    api(project(":api"))
    api(project(":lang"))

    compileOnly(libs.jetbrains.annotations)

    include(project(":api")) {
        isTransitive = false
    }
    include(project(":lang")) {
        isTransitive = false
    }

    modImplementation("me.lucko:fabric-permissions-api:0.3.3")
    modImplementation(libs.midnightcfg.minecraft)
    modImplementation("org.wallentines:databridge:0.6.0")
}