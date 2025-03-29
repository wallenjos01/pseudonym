plugins {
    id("build.library")
    id("build.publish")
}

dependencies {

    api(libs.midnightcfg.api)
    api(libs.midnightcfg.codec.nbt)
    api(project(":api"))

    compileOnly(libs.jetbrains.annotations)
    testImplementation(libs.midnightcfg.codec.json)
}