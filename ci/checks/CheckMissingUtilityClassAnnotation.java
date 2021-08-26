import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is {@link CheckMissingUtilityClassAnnotation}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckMissingUtilityClassAnnotation {
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
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Utils.java"))
            .forEach(file -> {
                var text = readFile(file);
                if (!text.contains("@UtilityClass")) {
                    print("%s is a utility class and yet is missing the @UtilityClass annotation", file);
                    failBuild.set(true);
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
