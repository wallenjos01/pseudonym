plugins {
    id("build.library")
    id("build.publish")
}

dependencies {

    api(libs.midnightcfg.api)
    compileOnly(libs.jetbrains.annotations)
}
