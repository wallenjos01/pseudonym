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
    val apiModules = listOf(
        "fabric-api-base",
        "fabric-resource-loader-v0"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))!!)
    }

    compileOnly(libs.jetbrains.annotations)

    include(project(":api"))
    include(project(":lang"))
    implementation(project(":api"))
    implementation(project(":lang"))

    modApi(include("org.wallentines:midnightcfg-api:3.0.1")!!)
}