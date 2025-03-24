plugins {
    id("build.library")
    id("build.publish")
}

dependencies {

    api("org.wallentines:midnightcfg-api:3.1.0")
    api("org.wallentines:midnightcfg-codec-nbt:3.1.0")
    api(project(":api"))

    compileOnly(libs.jetbrains.annotations)
}