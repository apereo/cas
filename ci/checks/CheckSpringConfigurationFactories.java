import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class CheckSpringConfigurationFactories {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    public static void main(final String[] args) throws Exception {
        checkSpringFactoryConfigurations(args[0]);
        checkMissingSpringFactoryConfigurations(args[0]);
        checkMissingSpringAutoConfigurations(args[0]);
    }

    private static void error(final String message, final Object... args) {
        //CHECKSTYLE:OFF
        System.err.printf(ANSI_RED + message + ANSI_RESET, args);
        System.err.println();
        //CHECKSTYLE:ON
    }

    private static String readFile(final Path file) {
        if (!file.toFile().exists()) {
            error("File %s does not exist", file);
            System.exit(1);
        }

        try {
            return Files.readString(file);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean checkProjectContainsSpringConfigurations(final String projectPath,
                                                                    final File springFactoriesFile,
                                                                    final String annotation) throws Exception {
        if (springFactoriesFile.exists()) {
            return true;
        }
        var pass = new AtomicBoolean(true);
        Files.walk(Paths.get(projectPath))
            .filter(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
                if (readFile(file).contains(annotation)) {
                    pass.set(false);
                }
            });
        return pass.get();
    }

    private static List<File> getSpringConfigurationFiles(final String projectPath, final String configurations, final String splitBy) {
        var list = new ArrayList<File>();
        var classes = configurations.split(splitBy);
        for (var it : classes) {
            var sourcePath = "/src/main/java/".replace("/", String.valueOf(File.separator)).trim();
            var clazz = projectPath + sourcePath + it.trim().replace(".", String.valueOf(File.separator)) + ".java";
            list.add(new File(clazz));
        }
        return list;
    }

    private static boolean checkForSpringConfigurationFactories(final String projectPath,
                                                                final String configurations,
                                                                final File springFactoriesFile,
                                                                final String splitBy) {
        var classes = getSpringConfigurationFiles(projectPath, configurations, splitBy);
        for (var it : classes) {
            if (!it.exists()) {
                error("Spring configuration class %s does not exist in %s", it, springFactoriesFile);
                return false;
            }
        }
        return true;
    }
    
    protected static void checkMissingSpringFactoryConfigurations(final String arg) throws IOException {
        var count = new AtomicInteger(0);
        var configurationNames = new HashSet<String>();

        Files.walk(Paths.get(arg))
            .filter(f -> Files.isRegularFile(f)
                && f.toFile().getName().endsWith("Configuration.java")
                && !f.toFile().getName().contains("Bootstrap"))
            .forEach(file -> {
                var text = readFile(file);
                if (text.contains("@AutoConfiguration")) {
                    var name = file.toFile().getName().replace(".java", "");
                    if (!name.startsWith("Cas")) {
                        error("Configuration class %s must start with 'Cas'", file.toFile().getAbsolutePath());
                        count.incrementAndGet();
                    }

                    var parent = file.getParent();
                    while (parent != null && !parent.toFile().getName().equals("src")) {
                        parent = parent.getParent();
                    }
                    var springFactoriesFile = new File(parent.toFile(),
                        "main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
                    if (!springFactoriesFile.exists()) {
                        error("Configuration class %s is missing from %s",
                            file.toFile().getAbsolutePath(), springFactoriesFile.getAbsolutePath());
                        count.incrementAndGet();
                    }

                    var className = file.toFile().getName().replace(".java", "");
                    if (file.toFile().getName().endsWith("AutoConfiguration.java")) {
                        var imports = readFile(springFactoriesFile.toPath());
                        if (!imports.contains(className)) {
                            error("AutoConfiguration class %s is not registered with Spring Boot's %s",
                                file.toFile().getAbsolutePath(), springFactoriesFile);
                            count.incrementAndGet();
                        }
                    }
                }
                if (text.contains("@Configuration")) {
                    var classname = file.toFile().getName().replace(".java", "");

                    if (text.contains("@Configuration(proxyBeanMethods = false)")) {
                        error("Configuration class %s must be uniquely identified with the name %s", file.toFile().getAbsolutePath(), classname);
                        count.incrementAndGet();
                    }

                    var matcher = Pattern.compile("@Configuration\\(value = \"(\\w+)\".+").matcher(text);
                    while (matcher.find()) {
                        var name = matcher.group(1);
                        if (!configurationNames.add(name)) {
                            error("Configuration class %s contains a duplicate name %s", file.toFile().getAbsolutePath(), name);
                            count.incrementAndGet();
                        }
                    }

                    if (!text.contains("AutoConfigurationRequired")) {
                        var autoconfig = Arrays.asList(file.getParent().toFile().listFiles(ff -> ff.getName().endsWith("AutoConfiguration.java")));
                        var noneMatch = autoconfig.stream().noneMatch(f -> {
                            var autoText = readFile(f.toPath());
                            return autoText.contains(classname);
                        });
                        if (noneMatch) {
                            error("Configuration class %s is not imported by auto configuration(s) %s", classname, autoconfig.toArray());
                            count.incrementAndGet();
                        }
                    }

                    if (text.contains("public static class " + classname)) {
                        error("Configuration class %s must be package-private; Remove public modifier from class definition", classname);
                        count.incrementAndGet();
                    }
                }
            });

        if (count.intValue() > 0) {
            System.exit(1);
        }
    }

    private static void checkMissingSpringAutoConfigurations(final String arg) throws IOException {
        var count = new AtomicInteger(0);

        try (var results = Files.list(Paths.get(arg))) {
            results.filter(path -> path.toFile().isDirectory()).forEach(path -> {
                try {
                    var sourceDir = new File(path.toFile(), "src/main/java");
                    var files = sourceDir.exists()
                        ? Files.walk(sourceDir.toPath())
                        .filter(srcFile -> Files.isRegularFile(srcFile)
                            && srcFile.toFile().getName().endsWith("Configuration.java")
                            && !srcFile.toFile().getName().endsWith("AutoConfiguration.java")
                            && readFile(srcFile).contains("@AutoConfiguration")).toList()
                        : List.<Path>of();

                    files.forEach(ff -> {
                        var foundFile = ff.toFile();
                        var classname = foundFile.getName().replace(".java", "");
                       
                        var newClassName = classname.replace("Configuration", "AutoConfiguration");
                        error("Configuration class %s must be renamed to %s", classname, newClassName);
                        count.incrementAndGet();
                    });
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if (count.intValue() > 0) {
            System.exit(1);
        }
    }

    protected static void checkSpringFactoryConfigurations(final String arg) throws IOException {
        var count = new AtomicInteger(0);

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isDirectory(file) && file.toFile().getAbsolutePath().endsWith("src/main/resources/META-INF"))
            .forEach(dir -> {
                try {
                    var projectPath = dir.getParent().getParent().getParent().getParent().toFile().getPath();
                    var springFactoriesFile = new File(dir.toFile(), "spring.factories");
                    var autoConfigImportFile = new File(dir.toFile(), "spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");

                    if (autoConfigImportFile.exists()) {
                        var classes = readFile(autoConfigImportFile.toPath());
                        if (!checkForSpringConfigurationFactories(projectPath, classes, autoConfigImportFile, "\n")) {
                            count.incrementAndGet();
                        }

                        var configFiles = getSpringConfigurationFiles(projectPath, classes, "\n");
                        configFiles.forEach(file -> {
                            var text = readFile(file.toPath());
                            if (text.contains("@Configuration") && !text.contains("@AutoConfiguration")) {
                                error("Configuration class %s is registered as an AutoConfiguration in %s",
                                    file.getAbsolutePath(), springFactoriesFile.getAbsolutePath());
                                count.incrementAndGet();
                            }
                        });
                    }

                    if (springFactoriesFile.exists()) {
                        var properties = new Properties();

                        try (var reader = new FileReader(springFactoriesFile)) {
                            properties.load(reader);
                        }

                        if (properties.isEmpty()) {
                            error("spring.factories file %s is empty", springFactoriesFile);
                            count.incrementAndGet();
                        }

                        if (properties.containsKey("org.springframework.cloud.bootstrap.BootstrapConfiguration")) {
                            var classes = (String) properties.get("org.springframework.cloud.bootstrap.BootstrapConfiguration");
                            if (!checkForSpringConfigurationFactories(projectPath, classes, springFactoriesFile, ",")) {
                                count.incrementAndGet();
                            }
                        }
                        if (properties.containsKey("org.springframework.boot.autoconfigure.EnableAutoConfiguration")) {
                            var classes = (String) properties.get("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
                            if (!checkForSpringConfigurationFactories(projectPath, classes, springFactoriesFile, ",")) {
                                count.incrementAndGet();
                            }

                            var configFiles = getSpringConfigurationFiles(projectPath, classes, ",");
                            configFiles.forEach(file -> {
                                var text = readFile(file.toPath());
                                if (text.contains("@Configuration") && !text.contains("@AutoConfiguration")) {
                                    error("Configuration class %s is registered as an AutoConfiguration in %s",
                                        file.getAbsolutePath(), springFactoriesFile.getAbsolutePath());
                                    count.incrementAndGet();
                                }
                            });
                        }

                    } else {
                        var c1 = checkProjectContainsSpringConfigurations(projectPath, springFactoriesFile, "@Configuration");
                        var c2 = checkProjectContainsSpringConfigurations(projectPath, autoConfigImportFile, "@AutoConfiguration");
                        if (!c1 && !c2) {
                            count.incrementAndGet();
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
