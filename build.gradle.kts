import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
    application
}

group = "ru.rofleksey.roflboard"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("net.jthink:jaudiotagger:3.0.1")
    implementation(files("lib/TarsosDSP-2.4.jar"))
}

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jmods")
}

task("buildInstaller", Exec::class) {
    dependsOn("build", "copyDependencies")
    commandLine("cmd", "/c", "buildInstaller.bat")
}

//task("copyJar", Copy::class) {
//    from(tasks.jar).into("$buildDir/jmods")
//}

//tasks.jpackage {
//    dependsOn("build", "copyDependencies", "copyJar")
//
//    appName = "RoflBoard"
//    appVersion = project.version.toString()
//    vendor = "ru.rofleksey.roflboard"
//    copyright = "Copyright (c) 2020 Vendor"
//    runtimeImage = System.getProperty("java.home")
//    module = "RoflBoard/ru.rofleksey.roflboard.Main"
//    modulePaths = listOf("$buildDir/jmods", System.getProperty("java.home")+"/jmods")
//    destination = "$buildDir/dist"
//    javaOptions = listOf("-Dfile.encoding=UTF-8")
//
//    windows {
//        winConsole = true
//        winMenu = true
//        winDirChooser = true
//        winShortcut = true
//    }
//}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("ru.rofleksey.roflboard.MainKt")
}