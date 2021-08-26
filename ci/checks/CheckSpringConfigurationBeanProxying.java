import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckSpringConfigurationBeanProxying}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckSpringConfigurationBeanProxying {
    public static void main(final String[] args) throws Exception {
        checkPattern(args[0]);
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

    protected static void checkPattern(final String arg) throws IOException {
        var failBuild = new AtomicBoolean(false);
        final var results = new ArrayList<>();

        var patternBeanMethods = Pattern.compile("public\\s\\w+(<\\w+>)*\\s(\\w+)\\(");

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
                var text = readFile(file);
                if (text.contains("@Configuration")) {
                    var canProxyBeans = true;
                    var matcher = patternBeanMethods.matcher(text);
                    results.clear();
                    while (matcher.find()) {
                        results.add(matcher.group(2));
                    }
                    for (var r : results) {
                        var patternMethodCall = Pattern.compile(r + "\\(");
                        var matcher2 = patternMethodCall.matcher(text);
                        var count = 0;
                        while (matcher2.find()) {
                            count++;
                        }
                        if (count > 1) {
                            canProxyBeans = false;
                            break;
                        }
                    }
                    if (canProxyBeans) {
                        var proxyPattern = Pattern.compile("@Configuration\\(value\\s*=\\s*\"(\\w+)\",\\s*proxyBeanMethods\\s*=\\s*(false|true)\\)").matcher(text);
                        if (!proxyPattern.find()) {
                            print("Configuration class %s should be marked with proxyBeanMethods = false", file);
                            failBuild.set(true);
                        }
                    }
                }

            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
