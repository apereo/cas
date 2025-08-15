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
        var patternBootTestClasses = Pattern.compile("@SpringBootTest\\(classes\\s*=\\s*\\{(.*?)\\}", Pattern.DOTALL);
        var patternBootTestClass = Pattern.compile("@SpringBootTest\\(classes\\s*=\\s*(.*)");
        var importedTestClasses = Pattern.compile("@Import\\(\\{(.*?)\\}\\)", Pattern.DOTALL);
        checkPattern(args[0], patternBootTestClasses);
        checkDuplicateTestConfiguration(args[0], patternBootTestClasses, importedTestClasses);
        checkCasTestExtension(args[0], patternBootTestClasses, patternBootTestClass);
    }
    
    private static void checkCasTestExtension(final String arg, final Pattern... patternBootTestClasses) throws Exception {
        var failBuild = new AtomicBoolean(false);
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
            .forEach(file -> {
                var text = readFile(file);
                Arrays.stream(patternBootTestClasses)
                    .forEach(pattern -> {
                        var matcher = pattern.matcher(text);
                        if (matcher.find() && !text.contains("CasTestExtension")) {
                            print("Class %s must be annotated with @ExtendWith(CasTestExtension.class)", file.toFile().getName());
                            failBuild.set(true);
                        }
                    });
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }

    protected static void checkDuplicateTestConfiguration(final String arg, final Pattern... patterns) throws Exception {
        var failBuild = new AtomicBoolean(false);
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
            .forEach(file -> {
                var text = readFile(file);
                
                for (var patternClasses : patterns) {
                    var matcher = patternClasses.matcher(text);

                    while (matcher.find()) {
                        var testClasses = matcher.group(1)
                            .replaceAll("\n", "")
                            .trim()
                            .replaceAll("\\s", "")
                            .split(",");
                        var setOfClasses = new HashSet<>();
                        for (var testClass : testClasses) {
                            if (!setOfClasses.add(testClass)) {
                                print("Class %s is duplicated in %s", testClass, file.toFile().getName());
                                failBuild.set(true);
                            }
                        }
                        setOfClasses.clear();
                    }
                }
            });

        if (failBuild.get()) {
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

    private static String getFileSimpleName(final File file) {
        return file.getName().replace(".java", "");
    }

    protected static void checkPattern(final String arg, final Pattern... patterns) throws Exception {
        var failBuild = new AtomicBoolean(false);
        var parentClasses = new TreeMap<String, File>();

        var abstractClazzPattern1 = Pattern.compile("(public)* abstract class (\\w+)");
        var abstractClazzPattern2 = Pattern.compile("(public)* abstract static class (\\w+)");
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
            .forEach(file -> {
                var text = readFile(file);

                var matcher = abstractClazzPattern1.matcher(text);
                while (matcher.find()) {
                    parentClasses.put(matcher.group(2), file.toFile());
                }

                matcher = abstractClazzPattern2.matcher(text);
                while (matcher.find()) {
                    parentClasses.put(matcher.group(2), file.toFile());
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
                            print("Unable to find %s as a parent class in %s", group, file);
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
                                    print("\t-Found duplicate configuration %s in %s inherited from %s%n", claz, file, parent);
                                    it.remove();
                                    foundDups = true;
                                    failBuild.set(true);
                                }
                            }
                            if (foundDups) {
                                print("%nThe child class %s should be annotated as "
                                        + "@SpringBootTest(classes = %s.SharedTestConfiguration.class) "
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
