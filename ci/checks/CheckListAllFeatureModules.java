import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link CheckListAllFeatureModules}.
 * This program scans CAS Java files in a given directory for feature declarations
 * and lists all features along with their associated modules (formatted for puppeteer test).
 * Usage: java ci/checks/CheckListAllFeatureModules .
 * @author Hal Deadman
 * @since 7.3.0
 */
public class CheckListAllFeatureModules {
    private static final Pattern FEATURE_PATTERN = Pattern.compile(
        "feature\\s*=\\s*CasFeatureModule.FeatureCatalog\\.([\\w.]+)(?:\\s*,\\s*module\\s*=\\s*\"([^\"]+)\")?"
    );

    public static void main(final String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: ExtractCasFeatures <source-root>");
            System.exit(1);
        }
        Map<String, String> featuresModules = new TreeMap<>();
        Files.walk(Paths.get(args[0]))
            .filter(f -> !f.toString().endsWith("Tests.java"))
            .filter(f -> f.toString().endsWith(".java"))
            .forEach(f -> processFile(f, featuresModules));
        featuresModules.forEach((feature, module) -> {
            if (module != null) {
                String[] parts = feature.split(",");
                System.out.printf("    \"--CasFeatureModule.%s.%s.enabled=false\",%n", parts[0], module);
            } else {
                System.out.printf("    \"--CasFeatureModule.%s.enabled=false\",%n", feature);
            }
        });
    }

    private static void processFile(final Path file, final Map<String, String> featuresModules) {
        try {
            String content = Files.readString(file);
            Matcher matcher = FEATURE_PATTERN.matcher(content);

            while (matcher.find()) {
                String feature = matcher.group(1);
                String module = matcher.group(2);
                if (module != null) {
                    featuresModules.put(feature + "," + module, module);
                } else {
                    featuresModules.put(feature, null);
                }
            }
        } catch (final IOException e) {
            System.err.println("Error reading " + file + ": " + e.getMessage());
        }
    }
}
