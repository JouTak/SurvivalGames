package integration.server;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfig {
    /**
     * Path to SurvivalGames.jar relative to project root.
     * Built by `gradlew shadowJar`.
     */
    public String pluginJarPath = "build/libs/SurvivalGames.jar";

    /**
     * Directory containing additional plugin JARs to copy to the test server.
     * Populated by the Gradle `copyServerPlugins` task.
     * Place KotlinPlugins.jar here manually if it is not available via Maven.
     */
    public Path extraPluginsDir = Paths.get("build/server-plugins");
}
