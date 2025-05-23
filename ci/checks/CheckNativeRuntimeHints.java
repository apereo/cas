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
                print("Runtime hint %s does not exist in %s",
                    configurationFile.getAbsolutePath(), springFactoriesFile.getAbsolutePath());
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
    
    protected static void checkRuntimeHintsConfigurations(final String arg) throws IOException {
        var count = new AtomicInteger(0);

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isDirectory(file) && file.toFile().getAbsolutePath().endsWith("src/main/resources/META-INF/spring"))
            .forEach(dir -> {
                var projectPath = dir.getParent().getParent().getParent().getParent().getParent().toFile().getPath();
                try {
                    var factoriesFile = new File(dir.toFile(), "aot.factories");

                    if (factoriesFile.exists()) {
                        var properties = new Properties();

                        try (var reader = new FileReader(factoriesFile)) {
                            properties.load(reader);
                        }

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

        Files.walk(Paths.get(arg))
            .filter(Files::isRegularFile)
            .filter(file -> file.toFile().getAbsolutePath().matches(".*src/main/java/.+RuntimeHints\\.java"))
            .forEach(file -> {
                var projectPath = file.getParent().getParent().getParent().getParent().getParent().getParent().toFile().getPath();
                var factoriesFile = new File(projectPath, "resources/META-INF/spring/aot.factories");
                var runtimeHint = file.toFile().getName().replace(".java", "");
                if (!factoriesFile.exists()) {
                    print("aot.factories file %s does not exist. It must contain the following:", factoriesFile);
                    print("org.springframework.aot.hint.RuntimeHintsRegistrar=org.apereo.cas.nativex.%s"
                        .formatted(runtimeHint));
                    count.incrementAndGet();
                }
                var factories = readFile(factoriesFile.toPath());
                if (!factories.contains(runtimeHint)) {
                    print("aot.factories file %s does not reference %s", factoriesFile, runtimeHint);
                    count.incrementAndGet();
                }
            });
            
        if (count.intValue() > 0) {
            System.exit(1);
        }
    }
}
