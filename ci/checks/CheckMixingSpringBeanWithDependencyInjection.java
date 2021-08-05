import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link CheckSpringConfigurationFactories}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class CheckMixingSpringBeanWithDependencyInjection {
    public static void main(final String[] args) throws Exception {
        checkConfigurations(args[0]);
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

    private static void checkConfigurations(final String projectPath) throws Exception {
        var pass = new AtomicBoolean(true);
        var pattern = Pattern.compile("^.*@Qualifier\\(\"(.+)\"\\)$");
        Files.walk(Paths.get(projectPath))
            .filter(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
//                print("Examining file %s", file);
                var contents = readFile(file);
                var matcher = pattern.matcher(contents);
                while (matcher.find()) {
                    var match = ".*\\w+\\s+\\w+\\s+" + matcher.group(1) + "\\(.*";
                    var matcher2 = Pattern.compile(match).matcher(contents);
//                    print("\tLooking for match %s", match);
                    if (matcher2.find()) {
                        print("Found injected dependency in file %s: Dependency injection is declared as a @Bean method: %s",
                           file.toFile().getPath(), matcher2.group().trim());
                        pass.set(false);
                        break;
                    }
                }
            });
        if (!pass.get()) {
            System.exit(1);
        }
    }
}
