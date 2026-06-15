package integration.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PluginBuildUtils {

    public static Path getOrBuildPluginJar(String configuredPath) throws Exception {
        Path targetPath = Paths.get(configuredPath);
        if (Files.exists(targetPath)) {
            return targetPath;
        }

        System.out.println("⚠️ SurvivalGames jar not found at " + targetPath + ". Attempting to build via Gradle...");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String gradleWrapper = isWindows ? "gradlew.bat" : "./gradlew";

        // Avoid `clean` so compiled test classes from IDE are not wiped
        ProcessBuilder pb = new ProcessBuilder(gradleWrapper, "shadowJar", "-x", "test");
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[GRADLE BUILD] " + line);
            }
        }
        process.waitFor();

        if (!Files.exists(targetPath)) {
            throw new IllegalStateException("❌ SurvivalGames plugin jar was not created after 'gradlew shadowJar'");
        }
        return targetPath;
    }
}
