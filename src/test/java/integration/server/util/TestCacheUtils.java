package integration.server.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class TestCacheUtils {

    public static Path preparePurpurCache(Path tempPluginsDir, String testId) throws Exception {
        // Each test class gets its own isolated directory so that Docker root files
        // from one test do not interfere with another.
        Path purpurCache = Paths.get(System.getProperty("user.home"), ".survivalgames-test-cache", "purpur-data-" + testId);
        Files.createDirectories(purpurCache);

        Path cachedPluginsDir = purpurCache.resolve("plugins");
        if (Files.exists(cachedPluginsDir)) {
            Files.walk(cachedPluginsDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {
                        }
                    });
        }

        Files.createDirectories(cachedPluginsDir);
        Files.walk(tempPluginsDir).forEach(src -> {
            try {
                Path rel = tempPluginsDir.relativize(src);
                Path dest = cachedPluginsDir.resolve(rel);
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Could not copy " + src + " to cache: " + e.getMessage());
            }
        });

        System.out.println("💾 Purpur cache dir: " + purpurCache);
        return purpurCache;
    }

    public static void cleanTempDir(Path tempDir) {
        if (tempDir != null) {
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (Exception ignored) {
            }
        }
    }
}
