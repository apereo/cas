import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckRequiredModuleAnnotationReferences}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class CheckRequiredModuleAnnotationReferences {
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

    protected static void checkPattern(final String location) throws IOException {
        final var failBuild = new AtomicBoolean(false);
        var pattern = Pattern.compile("@RequiresModule\\(name = \"(.+)\"\\)");

        Files.walk(Paths.get(location))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getName().endsWith("Properties.java"))
            .forEach(file -> {
                var text = readFile(file);
                var matcher = pattern.matcher(text);

                while (matcher.find()) {
                    var module = matcher.group(1);
//                    print("Found module %s", module);

                    var match = List.of("api", "core", "docs", "support", "webapp")
                        .stream()
                        .map(mod -> new File(location, mod))
                        .map(f -> {
                            var modDir = new File(f, module);
//                            print("Checking for project %s", modDir);
                            return modDir;
                        })
                        .anyMatch(File::exists);
                    if (!match) {
                        print("Unable to locate required module %s", module);
                        failBuild.set(true);
                    }
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
