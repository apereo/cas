import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link CheckMissingPuppeteerConfig}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class CheckMissingPuppeteerConfig {
    public static void main(final String[] args) throws Exception {
        checkFiles(args[0]);
    }

    private static void print(final String message, final Object... args) {
        //CHECKSTYLE:OFF
        System.out.printf(message, args);
        System.out.println();
        //CHECKSTYLE:ON
    }

    private static String readFile(final Path file) {
        try {
            return Files.readString(file);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void checkFiles(final String arg) throws IOException {
        var count = new AtomicInteger(0);

        var initialDir = new File(arg, "ci/tests/puppeteer/scenarios/");
        Files.walk(Paths.get(initialDir.getAbsolutePath()), 1)
            .filter(path -> Files.isDirectory(path) && !path.endsWith("scenarios"))
            .forEach(dir -> {
                var scenario = dir.toFile();
                var scenarioConfig = new File(scenario, "script.json");
                if (!scenarioConfig.exists()) {
                    print("Unable to locate scenario configuration file " + scenarioConfig.getAbsolutePath());
                    count.incrementAndGet();
                }
                var script = new File(scenario, "script.js");
                if (!script.exists()) {
                    print("Unable to locate scenario script file " + script.getAbsolutePath());
                    count.incrementAndGet();
                }
            });
        if (count.intValue() > 0) {
            System.exit(1);
        }
    }
}
