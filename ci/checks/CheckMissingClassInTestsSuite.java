import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CheckMissingClassInTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckMissingClassInTestsSuite {
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
        final var failBuild = new AtomicBoolean(false);
        final var count = new AtomicInteger(0);

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isDirectory(file) && file.toFile().getPath().endsWith("src/test/java"))
            .forEach(dir -> {
                try {
                    var testSuites = Files.walk(dir).filter(file -> Files.isRegularFile(file)
                        && file.toFile().getPath().matches(".*TestsSuite\\.java"))
                        .collect(Collectors.toList());

                    var testClasses = Files.walk(dir).filter(file -> Files.isRegularFile(file)
                        && file.toFile().getPath().matches(".*Tests\\.java")
                        && !file.toFile().getPath().matches(".*(Base|Abstract).+Tests\\.java"))
                        .collect(Collectors.toList());

                    if (testClasses.size() > 1 && testSuites.isEmpty()) {
                        print("Project %s is missing a TestsSuite class, while it contains %s tests", dir, testClasses.size());
                        failBuild.set(true);
                    }

                    if (testSuites.size() > 1) {
                        print("Project %s has more than one TestsSuite");
                        failBuild.set(true);
                    }

                    if (!testSuites.isEmpty()) {
                        var testSuiteFile = testSuites.get(0).toFile();
                        var testSuiteFileBody = readFile(testSuiteFile.toPath());
                        var missingClasses = testClasses.stream().filter(it -> {
                            var testClass = it.toFile().getName().replace(".java", ".class");
                            var patternMulti = Pattern.compile("\\s+" + testClass);
                            var patternSingle = Pattern.compile("@SelectClasses\\(" + testClass + "\\)");

                            return !patternMulti.matcher(testSuiteFileBody).find() && !patternSingle.matcher(testSuiteFileBody).find();
                        }).collect(Collectors.toList());
                        if (!missingClasses.isEmpty()) {
                            print("%n%s does not include:%n", testSuiteFile);
                            print(missingClasses
                                .stream()
                                .sorted()
                                .map(it -> it.toFile().getName().replace(".java", ".class"))
                                .collect(Collectors.joining("%n")));
                            count.set(count.intValue() + missingClasses.size());
                            System.exit(1);
                            failBuild.set(true);
                        }
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
