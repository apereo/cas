import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckDuplicateGradleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class CheckDuplicateGradleConfiguration {
    public static void main(final String[] args) throws Exception {
        checkUnusedDependencies(args[0]);
        checkPattern(args[0]);
    }

    private static void checkUnusedDependencies(final String arg) throws Exception {
        var libraries = new HashSet<String>();
        var dependencies = readFile(Paths.get(arg + "/gradle/dependencies.gradle"));
        var matcher = Pattern.compile("\\s*(\\w+)\\s+:\\s").matcher(dependencies);
        while (matcher.find()) {
            libraries.add(matcher.group(1));
        }
        print("Total dependencies: " + libraries.size());
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith(".gradle"))
            .forEach(file -> {
                if (!libraries.isEmpty()) {
                    var text = readFile(file);
                    libraries.removeIf(lib -> text.contains("libraries." + lib));
                }
            });
        if (!libraries.isEmpty()) {
            print("Found unused dependencies: " + String.join(", ", libraries));
            System.exit(1);
        }
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
        var pattern = Pattern.compile("implementation\\s+(project.+)");
        var testPattern = Pattern.compile("testImplementation\\s+(project.+)");

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && "build.gradle".equals(file.toFile().getName()))
            .forEach(file -> {
                var text = readFile(file);
                var matcher = pattern.matcher(text);

                while (matcher.find()) {
                    var match = matcher.group(1);
                    var p2 = matcher.group().replace("(", "\\(").replace(")", "\\)");
                    if (Pattern.compile(p2).matcher(text).results().count() > 1) {
                        print("\tFound duplicated configuration for %s at %s", match, file);
                        failBuild.set(true);
                    }

                    var testImpl = "testImplementation " + match;
                    if (text.contains(testImpl)) {
                        print("\tFound duplicate test configuration for %s at %s", testImpl, file);
                        failBuild.set(true);
                    }
                }

                matcher = testPattern.matcher(text);
                while (matcher.find()) {
                    var p2 = matcher.group().replace("(", "\\(").replace(")", "\\)");
                    var compiled = Pattern.compile(p2);
                    if (compiled.matcher(text).results().count() > 1) {
                        print("\tFound duplicate test configuration for %s at %s", matcher.group(), file);
                        failBuild.set(true);
                    }
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
