import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is {@link CheckMissingTestTagAnnotation}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckMissingTestTagAnnotation {
    public static void main(final String[] args) throws Exception {
        checkPattern(args[0]);
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
    protected static void checkPattern(final String arg) throws IOException {
        var failBuild = new AtomicBoolean(false);
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file)
                && file.toFile().getPath().matches(".*Tests\\.java")
                && !file.toFile().getPath().matches(".*(Base|Abstract).+Tests\\.java")
                && !readFile(file).contains("@Tag"))
            .forEach(file -> {
                print("%s must be assigned a test category via @Tag()", file);
                failBuild.set(true);
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
