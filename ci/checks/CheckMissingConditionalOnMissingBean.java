import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This is {@link CheckMissingConditionalOnMissingBean}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class CheckMissingConditionalOnMissingBean {
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
        var pattern = Pattern.compile("public class .+Configuration", Pattern.DOTALL);

        var patternWebflowAction = Pattern.compile("public Action (\\w+)");

        var webflowConsts = readFile(Paths.get("./api/cas-server-core-api-webflow/src/main/java/org/apereo/cas/web/flow/CasWebflowConstants.java"));
        
            
        Files.walk(Paths.get(arg))
            .filter(f -> Files.isRegularFile(f) && f.toFile().getName().endsWith("Configuration.java"))
            .forEach(file -> {
//                System.out.println(file.toFile().getName());
                var text = readFile(file);
                if (text.contains("@Configuration") && pattern.matcher(text).find()) {
//                    print("Checking file %s%n", file.getFileName());

                    var matcher = patternWebflowAction.matcher(text);
                    while (matcher.find()) {
                        var beanName = matcher.group(1);
                        var condition = String.format("@ConditionalOnMissingBean(name = \"%s\")", beanName);
                        if (text.contains(condition)) {
                            print("- Spring webflow action [%s] found in %s must be defined as a constant.%n",
                                beanName, file.getFileName().toString());
                            print("The @Bean definition must be annotated with: "
                                  + "@ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_...)%n");
                            failBuild.set(true);
                        }


                        var beanIdPattern = "String (ACTION_ID_.+) = \"(" + beanName + ")\"";
                        var beanMatcher = Pattern.compile(beanIdPattern).matcher(webflowConsts);
                        if (!beanMatcher.find()) {
                            print("- Unable to locate webflow action bean name/identifier %s in %s.%n", beanName, file.getFileName());
                            failBuild.set(true);
                        } else {
                            beanIdPattern = "@ConditionalOnMissingBean\\(name = CasWebflowConstants." + beanMatcher.group(1) + "\\)";
                            var conditionalMatcher = Pattern.compile(beanIdPattern).matcher(text);
                            if (!conditionalMatcher.find()) {
                                print("- Webflow action bean %s must be marked as conditional in %s.%n", beanName, file.getFileName());
                                print("The @Bean definition must be annotated with: %s%n%n", beanIdPattern.replace("\\", ""));
                                failBuild.set(true);
//                                System.exit(1);
                            }
                        }
                    }
                }
            });
        if (failBuild.get()) {
            System.exit(1);
        }
    }
}
