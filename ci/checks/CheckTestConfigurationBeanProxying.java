import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import module java.base;

public class CheckTestConfigurationBeanProxying {
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
        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Tests.java"))
            .forEach(file -> {
                var text = readFile(file);
                if (text.contains("@TestConfiguration")) {
                    // Check for @TestConfiguration("value") without proxyBeanMethods
                    var proxyPattern = Pattern.compile("@TestConfiguration\\(\"(\\w+)\"\\)").matcher(text);
                    if (proxyPattern.find()) {
                        print("TestConfiguration class %s should be marked with proxyBeanMethods = false. "
                            + "Use @TestConfiguration(value = \"%s\", proxyBeanMethods = false)%n", file, proxyPattern.group(1));
                        failBuild.set(true);
                    }
                    // Check for @TestConfiguration(value = "...", proxyBeanMethods = true)
                    proxyPattern = Pattern.compile("@TestConfiguration\\(value\\s*=\\s*\"(\\w+)\",\\s*proxyBeanMethods\\s*=\\s*(true)\\)").matcher(text);
                    if (proxyPattern.find()) {
                        print("TestConfiguration class %s should be marked with proxyBeanMethods = false%n", file);
                        failBuild.set(true);
                    }
                    // Check for @TestConfiguration(value = "...") without proxyBeanMethods
                    proxyPattern = Pattern.compile("@TestConfiguration\\(value\\s*=\\s*\"(\\w+)\"\\)").matcher(text);
                    if (proxyPattern.find()) {
                        print("TestConfiguration class %s should be explicitly marked with proxyBeanMethods = false%n", file);
                        failBuild.set(true);
                    }
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
