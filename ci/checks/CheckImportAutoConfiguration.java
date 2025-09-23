import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class CheckImportAutoConfiguration {

    private static final Pattern IMPORT_PATTERN = Pattern.compile("\\s*@Import\\(\\{(.+?)\\}\\)", Pattern.DOTALL);
    private static final Pattern IMPORT_AUTOCONFIG_PATTERN = Pattern.compile("\\s*@ImportAutoConfiguration\\(\\{(.+?)\\}\\)", Pattern.DOTALL);

    public static void main(final String[] args) throws Exception {
        checkPattern(args[0], IMPORT_AUTOCONFIG_PATTERN, IMPORT_PATTERN);
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

    protected static void checkPattern(final String arg, final Pattern autoConfigurationImport,
                                       final Pattern importPattern) throws IOException {
        var failBuild = new AtomicBoolean(false);
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
            .forEach(file -> {
                var text = readFile(file);
                var importMatcher = autoConfigurationImport.matcher(text);
                while (importMatcher.find()) {
                    var importedClasses = importMatcher.group(1);
                    var testConfig = Arrays.stream(importedClasses.split(","))
                        .filter(clz -> clz.contains("TestConfiguration"))
                        .findFirst();
                    if (testConfig.isPresent()) {
                        print("Use @Import for %s (or rename it) in file %s instead of @ImportAutoConfiguration%n", testConfig.get().trim(), file);
                        failBuild.set(true);
                    }
                }

                importMatcher = importPattern.matcher(text);
                while (importMatcher.find()) {
                    var importedClasses = importMatcher.group(1);
                    var testConfig = Arrays.stream(importedClasses.split(","))
                        .filter(clz -> clz.contains("AutoConfiguration"))
                        .findFirst();
                    if (testConfig.isPresent()) {
                        print("Use @ImportAutoConfiguration for %s (or rename it) in file %s instead of @Import%n", testConfig.get().trim(), file);
                        failBuild.set(true);
                    }
                }

            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
