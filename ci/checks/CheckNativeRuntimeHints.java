import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link CheckNativeRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CheckNativeRuntimeHints {
    public static void main(final String[] args) throws Exception {
        checkRuntimeHintsConfigurations(args[0]);
        checkMissingSpringFactoryConfigurations(args[0]);
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

    private static boolean checkForRuntimeHintsFactories(final String projectPath,
                                                         final String configurations,
                                                         final File springFactoriesFile,
                                                         final String splitBy) {
        var classes = configurations.split(splitBy);
        for (var it : Arrays.asList(classes)) {
            var sourcePath = "/src/main/java/".replace("/", String.valueOf(File.separator)).trim();
            var clazz = projectPath + sourcePath + it.trim().replace(".", String.valueOf(File.separator)) + ".java";
            var configurationFile = new File(clazz);
            if (!configurationFile.exists()) {
                print("Runtime hint %s does not exist in %s", clazz, springFactoriesFile);
                return false;
            }

            var text = readFile(configurationFile.toPath());
            if (!text.contains("package org.apereo.cas.nativex;")) {
                print("Runtime hint %s must be part of the 'org.apereo.cas.nativex' package.", clazz);
                return false;
            }
        }
        return true;
    }

    protected static void checkMissingSpringFactoryConfigurations(final String arg) throws IOException {
        Files.walk(Paths.get(arg))
            .filter(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
                if (readFile(file).contains("@Configuration")) {
                    var parent = file.getParent();
                    while (parent != null && !parent.toFile().getName().equals("src")) {
                        parent = parent.getParent();
                    }
                    var springFactoriesFile = new File(parent.toFile(),
                        "main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
                    if (!springFactoriesFile.exists()) {
                        print("Configuration class %s is missing from %s",
                            file.toFile().getAbsolutePath(), springFactoriesFile.getAbsolutePath());
                        System.exit(1);
                    }
                }
            });
    }

    protected static void checkRuntimeHintsConfigurations(final String arg) throws IOException {
        var count = new AtomicInteger(0);

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isDirectory(file) && file.toFile().getAbsolutePath().endsWith("src/main/resources/META-INF/spring"))
            .forEach(dir -> {
                try {
                    var projectPath = dir.getParent().getParent().getParent().getParent().getParent().toFile().getPath();
                    var factoriesFile = new File(dir.toFile(), "aot.factories");

                    if (factoriesFile.exists()) {
                        var properties = new Properties();

                        properties.load(new FileReader(factoriesFile));

                        if (properties.isEmpty()) {
                            print("aot.factories file %s is empty", factoriesFile);
                            count.incrementAndGet();
                        }

                        if (properties.containsKey("org.springframework.aot.hint.RuntimeHintsRegistrar")) {
                            var classes = (String) properties.get("org.springframework.aot.hint.RuntimeHintsRegistrar");
                            if (!checkForRuntimeHintsFactories(projectPath, classes, factoriesFile, ",")) {
                                count.incrementAndGet();
                            }
                        }
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });

        if (count.intValue() > 0) {
            System.exit(1);
        }
    }
}
