import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckRedundantTestConfigurationInheritance}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckRedundantTestConfigurationInheritance {
    public static void main(final String[] args) throws Exception {
        checkPattern(args[0],
            Pattern.compile("@SpringBootTest\\(classes\\s*=\\s*\\{(.*?)\\}", Pattern.DOTALL));
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

    private static String getFileSimpleName(final File file) {
        return file.getName().replace(".java", "");
    }

    protected static void checkPattern(final String arg,
                                       final Pattern... patterns) throws IOException {
        var failBuild = new AtomicBoolean(false);
        var abstractClazzPattern = Pattern.compile("public abstract class (\\w+)");
        var parentClasses = new TreeMap<String, File>();

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
            .forEach(file -> {
                var text = readFile(file);
                var matcher = abstractClazzPattern.matcher(text);
                while (matcher.find()) {
                    parentClasses.put(matcher.group(1), file.toFile());
                }
            });

        var pattern = Pattern.compile("public class (\\w+) extends (\\w+)");
        for (var patternClasses : patterns) {
            Files.walk(Paths.get(arg))
                .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
                .forEach(file -> {
                    var text = readFile(file);

                    var matcher = pattern.matcher(text);
                    if (matcher.find() && text.contains("@SpringBootTest")) {
                        var group = matcher.group(2);
                        if (!parentClasses.containsKey(group)) {
                            print("Unable to find %s as a parent class", group);
                            System.exit(1);
                        }
                        var parent = parentClasses.get(group);
                        var parentText = readFile(parent.toPath());
                        if (parentText.contains("@SpringBootTest")) {
                            var parentTestClasses = new HashSet<>();
                            var childTestClasses = new HashSet<>();

                            var classesMatcher = patternClasses.matcher(parentText);
                            if (classesMatcher.find()) {
                                var match = classesMatcher.group(1)
                                    .replaceAll("\n", "").trim().replaceAll("\\s", "");
                                parentTestClasses = new HashSet<>(Arrays.asList(match.split(",")));
                            }

                            classesMatcher = patternClasses.matcher(text);
                            if (classesMatcher.find()) {
                                var match = classesMatcher.group(1)
                                    .replaceAll("\n", "").trim().replaceAll("\\s", "");
                                childTestClasses = new HashSet<>(Arrays.asList(match.split(",")));
                            }

                            var foundDups = false;
                            var it = childTestClasses.iterator();
                            while (it.hasNext()) {
                                var claz = it.next().toString().trim();
                                if (parentTestClasses.contains(claz)) {
                                    print("Found duplicate configuration %s in %s inherited from %s", claz, file, parent);
                                    it.remove();
                                    foundDups = true;
                                    failBuild.set(true);
                                }
                            }
                            if (foundDups) {
                                print("%nThe child class %s should be annotated as"
                                        + "@SpringBootTest(classes = %s.SharedTestConfiguration.class)"
                                        + "and must only contain required configuration for the test. Shared/duplicate test"
                                        + "configuration must be pushed to %s.SharedTestConfiguration"
                                        + "instead, if it's not already defined.",
                                    getFileSimpleName(file.toFile()),
                                    getFileSimpleName(parent),
                                    getFileSimpleName(parent));
                            }
                        }
                    }
                });
        }

        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
