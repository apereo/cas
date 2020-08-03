import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckEnforceLombokAnnotations}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckEnforceLombokAnnotations {
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

    private static void checkForGetterSetter(final AtomicBoolean failBuild,
                                             final Path file,
                                             final String text,
                                             final String type,
                                             final String variable) {
        var capitalized = variable.substring(0, 1).toUpperCase() + variable.substring(1);
        var patternAccess = Pattern.compile("(public|private|protected)\\s+"
            + type
            + "\\s+(get|set)" + capitalized
            + "\\(\\)\\s*\\{\\s*return (this\\.)*"
            + variable + ";\\s*\\}", Pattern.DOTALL);

        var accessMatcher = patternAccess.matcher(text);
        if (accessMatcher.find()) {
            print("%s should convert the getter/setter for variable [%s] to use Lombok's @Getter/@Setter", file, variable);
            failBuild.set(true);
        }
    }

    protected static void checkPattern(final String arg) throws IOException {
        var failBuild = new AtomicBoolean(false);
        var pattern = Pattern.compile("(private|protected)\\s+final\\s(\\w+<*\\w*>*)\\s+(\\w+);");

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file)
                && file.toFile().getPath().matches(".*\\.java"))
            .forEach(file -> {
                var text = readFile(file);
                var matcher = pattern.matcher(text);
                while (matcher.find()) {
                    var type = matcher.group(2);
                    var variable = matcher.group(3);
                    checkForGetterSetter(failBuild, file, text, type, variable);
                }
            });

        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
