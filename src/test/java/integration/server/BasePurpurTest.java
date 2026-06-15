package integration.server;

import com.github.t9t.minecraftrconclient.RconClient;
import integration.server.util.PluginBuildUtils;
import integration.server.util.TestCacheUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class BasePurpurTest {

    protected GenericContainer<?> purpur;
    protected Path tempPluginsDir;
    protected Path purpurCacheDir;
    protected ServerConfig config;
    protected RconClient rconClient;
    protected StringBuilder purpurLogs = new StringBuilder();

    @BeforeAll
    void setupPurpur() throws Exception {
        config = configureServer();

        tempPluginsDir = Files.createTempDirectory("sg-plugins");
        System.out.println("🧱 Plugins dir: " + tempPluginsDir);

        // 1️⃣ SurvivalGames plugin JAR
        Path pluginJar = PluginBuildUtils.getOrBuildPluginJar(config.pluginJarPath);
        System.out.println("🔍 Using SurvivalGames jar at: " + pluginJar.toAbsolutePath());
        Files.copy(pluginJar, tempPluginsDir.resolve("SurvivalGames.jar"), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("✅ Copied SurvivalGames.jar to plugins dir");

        // 2️⃣ Extra plugin JARs (Multiverse-Core, MiniGamesAPI, KotlinPlugins, etc.)
        // Populated by `gradle copyServerPlugins`.
        // To add KotlinPlugins: place KotlinPlugins.jar in build/server-plugins/ manually.
        if (Files.exists(config.extraPluginsDir)) {
            copyDirectory(config.extraPluginsDir, tempPluginsDir);
            System.out.println("📦 Copied extra plugins from " + config.extraPluginsDir);
        } else {
            System.out.println("⚠️ No extra plugins dir found at " + config.extraPluginsDir
                    + " — run `gradlew copyServerPlugins` or add KotlinPlugins.jar manually");
        }

        // 3️⃣ Purpur container (Paper 1.21.4, Java 21)
        purpurCacheDir = TestCacheUtils.preparePurpurCache(tempPluginsDir, getClass().getSimpleName());

        GenericContainer<?> purpurBuilder = new GenericContainer<>("itzg/minecraft-server:java21")
                .withEnv("EULA", "TRUE")
                .withEnv("TYPE", "PAPER")
                .withEnv("VERSION", "1.21.4")
                .withEnv("ENABLE_RCON", "true")
                .withEnv("RCON_PASSWORD", "test")
                .withEnv("RCON_PORT", "25575")
                .withEnv("DEBUG", "true")
                .withExposedPorts(25575)
                .withFileSystemBind(purpurCacheDir.toString(), "/data",
                        org.testcontainers.containers.BindMode.READ_WRITE)
                .withStartupTimeout(Duration.ofMinutes(8))
                .withLogConsumer(this::printPurpurLogFrame);

        purpur = purpurBuilder;

        // Retry startup — Minecraft server download can time out on first attempt
        int maxRetries = 5;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("🚀 Starting Purpur container (attempt " + attempt + "/" + maxRetries + ")...");
                purpur.start();
                lastException = null;
                break;
            } catch (Exception e) {
                lastException = e;
                System.err.println("❌ Attempt " + attempt + " failed: " + e.getMessage());
                try {
                    purpur.stop();
                } catch (Exception ignored) {
                }
                if (attempt < maxRetries) {
                    System.out.println("⏳ Waiting 10s before retry...");
                    TimeUnit.SECONDS.sleep(10);
                    purpur = purpurBuilder;
                }
            }
        }
        if (lastException != null) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "Purpur container failed to start after " + maxRetries + " attempts: " + lastException.getMessage());
        }

        // 4️⃣ RCON client
        String rconHost = purpur.getHost();
        int rconPort = purpur.getMappedPort(25575);
        System.out.println("🎮 RCON connecting to " + rconHost + ":" + rconPort);
        rconClient = RconClient.open(rconHost, rconPort, "test");
        System.out.println("✅ RCON authenticated");

        TimeUnit.SECONDS.sleep(5);
    }

    @AfterAll
    void tearDownPurpur() throws Exception {
        if (rconClient != null) {
            try {
                rconClient.close();
            } catch (Exception ignored) {
            }
        }

        if (purpur != null) {
            try {
                System.out.println("===== FINAL PURPUR LOG DUMP =====");
                System.out.println(purpur.getLogs());
            } catch (Exception ignored) {
            } finally {
                purpur.stop();
            }
        }

        TestCacheUtils.cleanTempDir(tempPluginsDir);
    }

    protected String rcon(String command) {
        try {
            System.out.println("📤 RCON: " + command);
            String response = rconClient.sendCommand(command);
            System.out.println("📥 Response: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("❌ RCON error: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    protected boolean logsContain(String text) {
        return purpurLogs.toString().contains(text);
    }

    protected abstract ServerConfig configureServer();

    private void printPurpurLogFrame(OutputFrame frame) {
        if (frame == null) return;

        String text = frame.getUtf8String();
        if (text == null || text.isBlank()) return;

        purpurLogs.append(text);

        String prefix = frame.getType() == OutputFrame.OutputType.STDERR
                ? "[PURPUR ERR] "
                : "[PURPUR OUT] ";

        for (String line : text.split("\\R")) {
            if (!line.isBlank()) {
                System.out.println(prefix + line);
            }
        }
    }

    private static void copyDirectory(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> {
            try {
                Path target = dest.resolve(src.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("📄 Copied plugin: " + target.getFileName());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Could not copy " + source + ": " + e.getMessage());
            }
        });
    }
}
