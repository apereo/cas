import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is {@link CheckAsyncAnnotationInInterface}.
 * Fail build if Async annotation is not in an interface.
 * This ensures interface exists for Async classes so Spring can use JDK proxy instead of cglib proxy.
 * @author Hal Deadman
 * @since 6.5.0
 */
public class CheckAsyncAnnotationInInterface {
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
                && file.toFile().getPath().matches(".*\\.java")
                && !file.toFile().getPath().matches(".*Tests\\.java")
                && readFile(file).contains("@Async")
                && !readFile(file).contains("public interface"))
            .forEach(file -> {
                print("%s must have an interface that has Async annotation in interface", file);
                failBuild.set(true);
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
