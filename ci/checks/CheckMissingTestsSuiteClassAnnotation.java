import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is {@link CheckMissingTestsSuiteClassAnnotation}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckMissingTestsSuiteClassAnnotation {
    public static void main(final String[] args) throws Exception {
        checkPattern(args[0]);
    }

    private static void print(final String message, final Object... args) {
        //CHECKSTYLE:OFF
        System.out.printf(message, args);
        //CHECKSTYLE:ON
    }

    private static String readFile(final Path file) {
        try {
            return Files.readString(file);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void checkPattern(final String arg) throws IOException {
        var failBuild = new AtomicBoolean(false);
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file)
                && file.toFile().getPath().contains("src/test/java")
                && file.toFile().getName().endsWith("TestsSuite.java"))
            .forEach(file -> {
                var text = readFile(file);
                if (text.contains("@RunWith")) {
                    print("%s contains a suite of tests that are tagged with @RunWith annotation", file);
                    failBuild.set(true);
                }
                if (!text.contains("@Suite")) {
                    print("%s contains a suite of tests that is missing the @Suite annotation", file);
                    failBuild.set(true);
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
