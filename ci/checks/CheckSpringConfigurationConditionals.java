import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckSpringConfigurationConditionals}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckSpringConfigurationConditionals {
    public static void main(final String[] args) throws Exception {
        checkPattern(args[0]);
        checkDuplicateConfigurationNames(args[0]);
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
        var pattern = Pattern.compile("@ConditionalOnProperty.+public (static)* class .+Configuration", Pattern.DOTALL);
        Files.walk(Paths.get(arg))
            .filter(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
                var text = readFile(file);
                if (text.contains("@Configuration") && !text.contains("@SuppressWarnings(\"ConditionalOnProperty\")")
                    && pattern.matcher(text).find()) {
                    print("- Using @ConditionalOnProperty on configuration classes found in %s is not allowed. "
                          + "Consider using a feature module if appropriate, or using the BeanSupplier/BeanCondition API "
                          + "to allow bean definitions to support refresh requests and application context refreshes."
                          + "If you determine that using @ConditionalOnProperty is appropriate for the use case at hand, "
                          + "you may annotate the class with @SuppressWarnings(\"ConditionalOnProperty\").%n%n", file.getFileName().toString());
                    failBuild.set(true);
                }

            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }

    protected static void checkDuplicateConfigurationNames(final String arg) throws IOException {
        var allConfigurationNames = new HashSet<String>();
        var failBuild = new AtomicBoolean(false);
        var pattern = Pattern.compile("@Configuration\\(value\\s+=\\s+\"(\\w+)\"");
        Files.walk(Paths.get(arg))
            .filter(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
                var text = readFile(file);
                var matcher = pattern.matcher(text);
                while (matcher.find()) {
                    var configName = matcher.group(1);
                    if (allConfigurationNames.contains(configName)) {
                        print("Found a duplicate configuration name %s in file %s", configName, file.toFile().getAbsolutePath());
                        failBuild.set(true);
                    } else {
                        allConfigurationNames.add(configName);
                    }
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
