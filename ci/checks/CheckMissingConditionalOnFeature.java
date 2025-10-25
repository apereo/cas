import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckMissingConditionalOnFeature}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class CheckMissingConditionalOnFeature {
    void main(final String[] args) throws Exception {
        checkPattern(args[0]);
    }

    private static void print(final String message, final Object... args) {
        //CHECKSTYLE:OFF
        System.out.print("\uD83C\uDFC1 ");
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
        var pattern = Pattern.compile("public class .+Configuration", Pattern.DOTALL);
        Files.walk(Paths.get(arg))
            .filter(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
//                System.out.println(file.toFile().getName());
                var text = readFile(file);
                if (text.contains("@Configuration") && pattern.matcher(text).find()
                    && !text.contains("@ConditionalOnFeature") && !text.contains("@Deprecated")) {
                    print("- Missing @ConditionalOnFeature in %s%n", file.getFileName().toString());
                    failBuild.set(true);
                }

            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
