import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import module java.base;

/**
 * This is {@link CheckSpringConfigurationBeanProxying}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckSpringConfigurationBeanProxying {
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
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith(".java"))
            .forEach(file -> {
                var text = readFile(file);
                if (text.contains("@Configuration")) {
                    var proxyPattern = Pattern.compile("@Configuration\\(value\\s*=\\s*\"(\\w+)\",\\s*proxyBeanMethods\\s*=\\s*(true)\\)").matcher(text);
                    if (proxyPattern.find()) {
                        print("Configuration class %s should be marked with proxyBeanMethods = false%n", file);
                        failBuild.set(true);
                    }
                    proxyPattern = Pattern.compile("@Configuration\\(value\\s*=\\s*\"(\\w+)\"\\)").matcher(text);
                    if (proxyPattern.find()) {
                        print("Configuration class %s should be explicitly marked with proxyBeanMethods = false%n", file);
                        failBuild.set(true);
                    }
                }


                if (text.contains("@AutoConfiguration") && file.toFile().getAbsolutePath().contains("src/main/java")) {
                    var packagePattern = Pattern.compile("package (.+);").matcher(text);
                    if (packagePattern.find()) {
                        var packageName = packagePattern.group(1);
                        if (!packageName.equals("org.apereo.cas.config")) {
                            print("Configuration class %s in package %s must be placed inside the package 'org.apereo.cas.config'%n", file, packageName);
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
