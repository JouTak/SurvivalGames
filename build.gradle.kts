group = "ru.joutak"
version = System.getProperty("version")
val commitHash = System.getProperty("commitHash")
if (commitHash.isNotBlank()) {
    version = "$version-$commitHash"
}

val targetJavaVersion = 21
plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// Plugin JARs to be copied to the test Minecraft server's plugins/ directory
val serverPlugins: Configuration by configurations.creating

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://maven.joutak.ru/snapshots")
    maven("https://maven.joutak.ru/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.onarandombox.multiversecore:multiverse-core:4.3.14")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("ru.joutak:minigamesapi:3.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.7")

    // Server plugin JARs placed next to SurvivalGames.jar in the test container
    serverPlugins("com.onarandombox.multiversecore:multiverse-core:4.3.14")
    serverPlugins("ru.joutak:minigamesapi:3.6.3")

    // Integration test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.testcontainers:testcontainers:1.20.1")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("com.github.t9t.minecraft-rcon-client:minecraft-rcon-client:1.0.0")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.9")
}

// Copy server plugin JARs to build/server-plugins/ for use in integration tests
tasks.register<Copy>("copyServerPlugins") {
    from(serverPlugins)
    into(layout.buildDirectory.dir("server-plugins"))
}

tasks.test {
    useJUnitPlatform()
    dependsOn("shadowJar", "copyServerPlugins")
}

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.shadowJar {
    archiveClassifier = ""
    archiveFileName.set("${project.name}.jar")

    val serverPath = System.getenv("SERVER_PATH")
    if (System.getenv("TESTING") != null) {
        if (serverPath != null) {
            destinationDirectory.set(file("$serverPath\\plugins"))
        } else {
            logger.warn("SERVER_PATH property is not set!")
        }
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
