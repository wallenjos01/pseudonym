plugins {
    id("build.library")
    id("build.publish")
}

dependencies {

    api(libs.midnightcfg.api)
    api(libs.midnightcfg.api.sql)
    api(libs.midnightcfg.codec.nbt)
    api(project(":api"))

    compileOnly(libs.jetbrains.annotations)
}
