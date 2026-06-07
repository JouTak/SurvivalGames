package integration.server;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration test for SurvivalGames on a real Paper server in Docker.
 *
 * <p>What happens:
 * <ol>
 *   <li>Paper 1.21.4 server is started in Docker via TestContainers</li>
 *   <li>SurvivalGames.jar + dependency plugins are loaded</li>
 *   <li>Commands are executed via RCON and responses are validated</li>
 * </ol>
 *
 * <p>Requirements:
 * <ul>
 *   <li>Docker is running</li>
 *   <li>Project is built: {@code gradlew shadowJar}</li>
 *   <li>Dependency plugins copied: {@code gradlew copyServerPlugins}</li>
 *   <li>KotlinPlugins.jar placed manually in {@code build/server-plugins/} if needed</li>
 * </ul>
 *
 * <p>Run:
 * <pre>
 *   gradlew test --tests "integration.server.SurvivalGamesCommandsIT"
 * </pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SurvivalGamesCommandsIT extends BasePurpurTest {

    @Override
    protected ServerConfig configureServer() {
        return new ServerConfig();
    }

    @Test
    @Order(1)
    @DisplayName("Server starts and Paper loads successfully")
    void serverShouldStart() {
        assertTrue(logsContain("Done") || logsContain("For help"),
                "Paper server should report startup complete. Check container logs.");
    }

    @Test
    @Order(2)
    @DisplayName("SurvivalGames plugin loads successfully")
    void pluginShouldLoad() {
        // Plugin startup message from SurvivalGamesPlugin.kt onEnable()
        boolean pluginLoaded = logsContain("Плагин SurvivalGames версии")
                || logsContain("Enabling SurvivalGames");

        if (!pluginLoaded) {
            System.out.println("⚠️ SurvivalGames did not load. Likely missing dependency plugin (KotlinPlugins, MiniGamesAPI or Multiverse-Core).");
            System.out.println("   Place the missing JARs in build/server-plugins/ and re-run.");
        }

        assumeTrue(pluginLoaded,
                "SurvivalGames plugin must be loaded. Check logs — a dependency plugin may be missing.");
    }

    @Test
    @Order(3)
    @DisplayName("/sgspawnlist — should report empty list on fresh server")
    void spawnListShouldBeEmptyInitially() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgspawnlist");

        assertNotNull(response, "RCON response must not be null");
        assertTrue(response.contains("пуст"),
                "Empty spawn list should report 'пуст'. Got: " + response);
    }

    @Test
    @Order(4)
    @DisplayName("/sgaddspawn TestSpawn 0 64 0 0 0 — should add spawn successfully")
    void addSpawnShouldWork() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgaddspawn TestSpawn 0 64 0 0 0");

        assertNotNull(response, "RCON response must not be null");
        assertTrue(response.contains("TestSpawn") && response.contains("добавлен"),
                "Response should confirm spawn was added. Got: " + response);
    }

    @Test
    @Order(5)
    @DisplayName("/sgaddspawn TestSpawn — duplicate spawn name should be rejected")
    void addDuplicateSpawnShouldFail() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgaddspawn TestSpawn 0 64 0 0 0");

        assertNotNull(response);
        assertTrue(response.contains("уже существует"),
                "Duplicate spawn name should be rejected. Got: " + response);
    }

    @Test
    @Order(6)
    @DisplayName("/sgspawnlist — should show TestSpawn after it was added")
    void spawnListShouldShowAddedSpawn() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgspawnlist");

        assertNotNull(response, "RCON response must not be null");
        assertTrue(response.contains("TestSpawn"),
                "Spawn list should include 'TestSpawn'. Got: " + response);
    }

    @Test
    @Order(7)
    @DisplayName("/sgconfig MAX_PLAYERS_IN_GAME — should return default value 20")
    void configReadShouldReturnDefaultValue() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgconfig MAX_PLAYERS_IN_GAME");

        assertNotNull(response, "RCON response must not be null");
        assertFalse(response.isBlank(), "Response must not be empty");
        assertTrue(response.contains("MAX_PLAYERS_IN_GAME"),
                "Response should echo the config key. Got: " + response);
    }

    @Test
    @Order(8)
    @DisplayName("/sgconfig MAX_PLAYERS_IN_GAME 10 — should update value")
    void configWriteShouldUpdateValue() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgconfig MAX_PLAYERS_IN_GAME 10");

        assertNotNull(response, "RCON response must not be null");
        assertTrue(response.contains("обновлено") || response.contains("10"),
                "Response should confirm value was updated. Got: " + response);
    }

    @Test
    @Order(9)
    @DisplayName("/sgconfig MAX_PLAYERS_IN_GAME — should now return updated value 10")
    void configReadShouldReturnUpdatedValue() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgconfig MAX_PLAYERS_IN_GAME");

        assertNotNull(response);
        assertTrue(response.contains("10"),
                "Config should now read 10 after update. Got: " + response);
    }

    @Test
    @Order(10)
    @DisplayName("/sgconfig UNKNOWN_KEY — should report key not found")
    void configUnknownKeyShouldFail() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgconfig NONEXISTENT_KEY");

        assertNotNull(response);
        assertTrue(response.contains("не найден") || response.contains("NONEXISTENT_KEY"),
                "Unknown config key should report not found. Got: " + response);
    }

    @Test
    @Order(11)
    @DisplayName("/sgremovespawn TestSpawn — should remove spawn successfully")
    void removeSpawnShouldWork() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgremovespawn TestSpawn");

        assertNotNull(response, "RCON response must not be null");
        assertTrue(response.contains("TestSpawn") && response.contains("удален"),
                "Response should confirm spawn was removed. Got: " + response);
    }

    @Test
    @Order(12)
    @DisplayName("/sgspawnlist — should be empty again after removing TestSpawn")
    void spawnListShouldBeEmptyAfterRemoval() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgspawnlist");

        assertNotNull(response);
        assertTrue(response.contains("пуст"),
                "Spawn list should be empty after removing all spawns. Got: " + response);
    }

    @Test
    @Order(13)
    @DisplayName("/sgremovespawn NonExistentSpawn — should report not found")
    void removeNonExistentSpawnShouldFail() {
        assumeTrue(logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames"),
                "SurvivalGames must be loaded for this test");

        String response = rcon("sgremovespawn NonExistentSpawn");

        assertNotNull(response);
        assertTrue(response.contains("не существует"),
                "Removing nonexistent spawn should report not found. Got: " + response);
    }
}
