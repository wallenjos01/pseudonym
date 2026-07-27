import buildlogic.Utils

plugins {
    id("build.fabric")
    id("build.publish")
}

Utils.setupResources(project, rootProject, "fabric.mod.json")

dependencies {

    minecraft("com.mojang:minecraft:${project.properties["minecraft-version"]}")
    implementation("net.fabricmc:fabric-loader:${project.properties["fabric-loader-version"]}")

    // Fabric API
    listOf(
        "fabric-api-base",
        "fabric-resource-loader-v0"
    ).forEach { mod ->
        api(include(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))!!)
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

    implementation(libs.midnightcfg.minecraft)
    implementation("org.wallentines:databridge:0.12.1")

    // Gametest API modules
    val testApiModules = listOf(
        "fabric-gametest-api-v1",
        "fabric-registry-sync-v0"
    )
    for(mod in testApiModules) {
        gametestImplementation(fabricApi.module(mod, "${project.properties["fabric-api-version"]}"))
    }
}
