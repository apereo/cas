import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckDuplicateTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckDuplicateTestConfiguration {

    public static void main(final String[] args) throws Exception {
        checkPattern(args[0],
            Pattern.compile("@SpringBootTest\\(classes\\s*=\\s*\\{(.*?)\\}", Pattern.DOTALL),
            Pattern.compile("\\s*@Import\\(\\{(.+?)\\}\\)", Pattern.DOTALL));
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

    protected static void checkPattern(final String arg,
                                       final Pattern... patterns) throws IOException {
        var failBuild = new AtomicBoolean(false);
        var duplicatesInTestClass = new TreeSet<>();

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
            .forEach(file -> {
                duplicatesInTestClass.clear();
                var text = readFile(file);
                Arrays.stream(patterns).forEach(pattern -> {
                    var matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        var match = matcher.group(1);
                        var classes = match.split(",");
                        Arrays.stream(classes).forEach(clz -> {
                            var className = clz.trim();
                            if (!duplicatesInTestClass.add(className)) {
                                print("Duplicate found: %s in %s %n", className, file);
                                failBuild.set(true);
                            }
                        });
                    }
                });

            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
