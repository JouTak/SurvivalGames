group = "ru.joutak"
version = System.getProperty("version")
val commitHash = System.getProperty("commitHash")
if (commitHash.isNotBlank()) {
    version = "$version-$commitHash"
}

val targetJavaVersion = 17
plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}


kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.shadowJar {
    archiveClassifier = ""
    archiveFileName.set("${project.name}.jar")
    if (System.getenv("TESTING") != null) {
        destinationDirectory.set(file("${layout.projectDirectory}/../1.20.1-server/plugins/"))
    }
}

tasks.jar {
    finalizedBy("shadowJar")
    enabled = false
}

tasks.processResources {
    val props = mapOf("version" to version, "pluginName" to project.name)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}