import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * This is {@link CheckFunctionalConditionUsage}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CheckFunctionalConditionUsage {
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
            throw new RuntimeException(file.toFile().getAbsolutePath(), e);
        }
    }

    protected static void checkPattern(final String arg) throws IOException {
        var patternBlankString = Pattern.compile("(?<!else)(\\s+)if \\(StringUtils.isNotBlank\\((\\w+\\.\\w+\\(\\))\\)\\)\\s\\{\\n\\s+(\\w+\\.set\\w+)\\(\\w+\\.\\w+\\(\\)\\);\\n\\s+}(?!\\selse)");

        var count = new AtomicLong();

        Files.walk(Paths.get(arg))
            .filter(file -> Files.isRegularFile(file) && file.toFile().getAbsolutePath().contains("src/main/java") && file.toFile().getName().endsWith(".java"))
            .forEach(file -> {
                
                var text = readFile(file);
                var matcher = patternBlankString.matcher(text);
                if (matcher.find()) {
                    var theFile = file.toFile();
                    print("%n- %s: %s", theFile.getAbsolutePath(), matcher.group());
                    print("%n%nStringUtils#isNotBlank() can be replaced with FunctionUtils#doIfNotBlank():\n");

                    var indentation = matcher.group(1);
                    var condition = matcher.group(2);
                    var setter = matcher.group(3);

                    var replacement = '\n' + indentation + "FunctionUtils.doIfNotBlank(" + condition + ", __ -> " + setter + '(' + condition + "));";
                    print("%s%n", replacement);
                    print("-----------------------------------------------------");
//                    var newText = text.replace(matcher.group(), replacement);
//                    writeFile(file, newText);
                    count.getAndIncrement();
                }
            });
        if (count.get() > 0) {
            System.exit(1);
        }
    }
}
