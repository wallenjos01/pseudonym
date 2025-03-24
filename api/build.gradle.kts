plugins {
    id("build.library")
    id("build.publish")
}

dependencies {

    api("org.wallentines:midnightcfg-api:3.0.1")
    compileOnly(libs.jetbrains.annotations)
}