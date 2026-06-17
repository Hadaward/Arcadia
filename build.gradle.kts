plugins {
    id("java")
}

val pluginName = property("plugin_name").toString()
val pluginVersion = property("plugin_version").toString()
val hytaleServerVersion = property("hytale_server_version").toString()

val voskVersion = property("vosk_version").toString()
val concentusVersion = property("concentus_version").toString()

group = "com.github.hadaward"
version = pluginVersion

repositories {
    mavenCentral()
    maven {
        name = "hytale"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:${hytaleServerVersion}")

    implementation("com.alphacephei:vosk:${voskVersion}")
    implementation("io.github.jaredmdobson:concentus:${concentusVersion}")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.21.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testCompileOnly("com.google.code.findbugs:jsr305:3.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveBaseName.set(pluginName)
    archiveVersion.set(pluginVersion)

    from("src/main/resources")

    from({
        configurations.runtimeClasspath.get()
            .filter {
                it.name.endsWith(".jar")
            }
            .map {
                zipTree(it)
            }
    })
}

tasks.test {
    useJUnitPlatform()
}