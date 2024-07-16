import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CheckPuppeteerScenarioScriptConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class CheckPuppeteerScenarioScriptConfiguration {
    public static void main(final String[] args) throws Exception {
        checkPattern(args[0]);
    }

    private static void print(final String message, final Object... args) {
        //CHECKSTYLE:OFF
        System.out.print("\uD83C\uDFC1 ");
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
                && file.toFile().getName().equals("script.json"))
            .forEach(file -> {
//                print(file.toFile().getAbsolutePath());
                var text = readFile(file);
                var matcher = Pattern.compile("\"properties\": \\[(.+)\\]", Pattern.DOTALL).matcher(text);
                if (matcher.find()) {
                    var properties = Arrays.stream(matcher.group(1)
                        .trim().split("\",")).map(String::trim).collect(Collectors.toList());
                    var unique = new HashSet<>(properties);
                    if (unique.size() != properties.size()) {
                        print("%s contains duplicate configuration settings.", file);
                        print("Duplicate settings are:", file);
                        var dups = properties.stream()
                            .filter(e -> Collections.frequency(properties, e) > 1)
                            .distinct()
                            .map(e -> e + '"')
                            .collect(Collectors.toList());
                        print("\t- %s %n", dups);
                        failBuild.set(true);
                    }
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
