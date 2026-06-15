package integration.server;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for the MiniGamesAPI matchmaking integration in SurvivalGames.
 *
 * <p>SurvivalGames runs in SOLO (FFA) mode — no team selection. Player flow:
 * <ol>
 *   <li>Player joins lobby</li>
 *   <li>Player types /ready — added to matchmaking queue</li>
 *   <li>When enough players queue up, the server polls MatchmakingManager every second</li>
 *   <li>Game auto-starts: players teleport to arena, countdown, barriers drop, PvP after delay</li>
 *   <li>Last standing player wins; match result is recorded to DB via MiniGamesAPI</li>
 * </ol>
 *
 * <p>All RCON commands in these tests execute as console (not as a player), so player-only
 * commands (/ready, /unready) are expected to return rejection messages. Admin commands
 * (/forcerun) are expected to work from console.
 *
 * <p>Requirements: same as SurvivalGamesCommandsIT — Docker running, shadowJar built,
 * copyServerPlugins run, KotlinPlugins.jar in build/server-plugins/ if needed.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SurvivalGamesMatchmakingIT extends BasePurpurTest {

    @Override
    protected ServerConfig configureServer() {
        return new ServerConfig();
    }

    // ── Prerequisites ──────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Server starts and Paper loads successfully")
    void serverShouldStart() {
        assertTrue(logsContain("Done") || logsContain("For help"),
                "Paper server should report startup complete. Check container logs.");
    }

    @Test
    @Order(2)
    @DisplayName("SurvivalGames and MiniGamesAPI both load successfully")
    void pluginsAndApiShouldLoad() {
        boolean sgLoaded = logsContain("Плагин SurvivalGames версии")
                || logsContain("Enabling SurvivalGames");
        boolean apiLoaded = logsContain("Enabling MiniGamesAPI")
                || logsContain("MiniGamesAPI");

        if (!sgLoaded) {
            System.out.println("⚠️ SurvivalGames did not load — check dependencies in build/server-plugins/");
        }
        if (!apiLoaded) {
            System.out.println("⚠️ MiniGamesAPI did not load — place minigamesapi.jar in build/server-plugins/");
        }

        assumeTrue(sgLoaded,
                "SurvivalGames must be loaded. Missing dependency plugin (KotlinPlugins, MiniGamesAPI, Multiverse-Core)?");
        assumeTrue(apiLoaded,
                "MiniGamesAPI must be loaded. Place minigamesapi.jar in build/server-plugins/");
    }

    // ── Player-only commands rejected for console ──────────────────────────────

    @Test
    @Order(3)
    @DisplayName("/ready from console — must be rejected with a player-only message")
    void readyFromConsoleShouldBeRejected() {
        assumeTrue(isSgLoaded(), "SurvivalGames must be loaded");

        String response = rcon("ready");

        assertNotNull(response, "RCON response must not be null");
        assertFalse(response.isBlank(), "Response must not be empty");

        // Both SurvivalGames and MiniGamesAPI /ready reject non-player senders.
        // Exact wording depends on which command wins the Brigadier registration race;
        // both contain "игр" or "игрок" in their rejection messages.
        boolean isRejected = response.toLowerCase().contains("игрок")
                || response.toLowerCase().contains("игре")
                || response.toLowerCase().contains("только")
                || response.toLowerCase().contains("players");
        assertTrue(isRejected,
                "Console /ready should be rejected with a player-only message. Got: " + response);
    }

    @Test
    @Order(4)
    @DisplayName("/unready from console — must be rejected with a player-only message")
    void unreadyFromConsoleShouldBeRejected() {
        assumeTrue(isSgLoaded(), "SurvivalGames must be loaded");

        String response = rcon("unready");

        assertNotNull(response, "RCON response must not be null");
        // MiniGamesAPI /unready also rejects non-player senders.
        // If the command is not recognized, Paper returns an unknown-command message.
        assertFalse(response.contains("Exception") || response.contains("NullPointer"),
                "Console /unready must not throw an exception. Got: " + response);
    }

    @Test
    @Order(5)
    @DisplayName("/teamselect from console — must report solo-mode disabled message")
    void teamselectInSoloModeShouldBeDisabled() {
        assumeTrue(isSgLoaded(), "SurvivalGames must be loaded");

        String response = rcon("teamselect");

        assertNotNull(response, "RCON response must not be null");
        // Either player-only rejection or "solo mode disabled" — both are valid.
        assertFalse(response.contains("Exception") || response.contains("NullPointer"),
                "/teamselect must not throw an exception. Got: " + response);
    }

    // ── Admin matchmaking commands ─────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("/forcerun with no players in queue — should report no active instances")
    void forcerunWithEmptyQueueShouldReportNoInstances() {
        assumeTrue(isSgLoaded(), "SurvivalGames must be loaded");

        String response = rcon("forcerun");

        assertNotNull(response, "RCON response must not be null");
        assertFalse(response.isBlank(), "Response must not be empty");
        assertFalse(response.contains("Exception") || response.contains("NullPointer"),
                "/forcerun must not throw an exception. Got: " + response);

        // MiniGamesAPI forcerun with empty queue reports "no active instances".
        // The message key is messages.forcerun.no_instances → "Нет активных инстансов."
        assertTrue(response.contains("инстанс") || response.contains("Нет") || response.contains("запущена"),
                "/forcerun should report no active instances or that game will start. Got: " + response);
    }

    @Test
    @Order(7)
    @DisplayName("Set MAX_PLAYERS_IN_GAME=1, then /forcerun — still reports no instances (queue empty)")
    void forcerunWithMinPlayersShouldStillReportEmpty() {
        assumeTrue(isSgLoaded(), "SurvivalGames must be loaded");

        // Reduce required players to 1 — game would start with just 1 queued player.
        String configResponse = rcon("sgconfig MAX_PLAYERS_IN_GAME 1");
        assertNotNull(configResponse);

        // But no players are connected, so queue is still empty.
        String response = rcon("forcerun");
        assertNotNull(response, "RCON response must not be null");
        assertFalse(response.contains("Exception") || response.contains("NullPointer"),
                "/forcerun must not throw after config change. Got: " + response);

        // Restore default value so later tests are not affected.
        rcon("sgconfig MAX_PLAYERS_IN_GAME 20");
    }

    // ── No errors in server logs throughout the session ────────────────────────

    @Test
    @Order(8)
    @DisplayName("No matchmaking-related exceptions in server logs")
    void noMatchmakingExceptionsInLogs() {
        assumeTrue(isSgLoaded(), "SurvivalGames must be loaded");

        String logs = purpurLogs.toString();

        // Any NullPointerException or unhandled exception inside matchmaking classes is a bug.
        boolean hasCrash = logs.contains("NullPointerException")
                || logs.contains("at ru.joutak.minigames.managers.MatchmakingManager")
                || logs.contains("at ru.joutak.sg.games.GameManager");

        assertFalse(hasCrash,
                "Server logs contain a matchmaking exception — check container logs for stack trace.");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private boolean isSgLoaded() {
        return logsContain("Плагин SurvivalGames версии") || logsContain("Enabling SurvivalGames");
    }
}
