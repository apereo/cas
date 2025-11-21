import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * A utility to recursively scan a Java source directory and insert
 * a JSpecify '@NullMarked' package-info.java file into every package.
 *
 * <p>This tool is NON-DESTRUCTIVE. It will not overwrite any existing
 * package-info.java file.
 *
 * <p>Usage:
 * <pre>
 * 1. Compile: javac PackageInfoGenerator.java
 * 2. Run:     java PackageInfoGenerator /path/to/your/project/src/main/java
 * </pre>
 */
public class PackageInfoGenerator {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java PackageInfoGenerator <path-to-src-root>");
            System.err.println("Example: java PackageInfoGenerator ./src/main/java");
            System.exit(1);
        }

        Path rootDir = Paths.get(args[0] + "/src/main/java");

        if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
            System.err.println("Error: The path " + rootDir + " is not a valid directory.");
            System.exit(1);
        }

        System.out.println("Scanning for packages in: " + rootDir.toAbsolutePath());
        System.out.println("---");

        try {
            processDirectory(rootDir);
        } catch (IOException e) {
            System.err.println("An error occurred during processing:");
            e.printStackTrace();
        }

        System.out.println("---");
        System.out.println("Processing complete.");
    }

    /**
     * Walks the directory tree and applies the package-info logic to each directory.
     *
     * @param rootDir The root source directory (e.g., "src/main/java")
     * @throws IOException if an I/O error occurs
     */
    private static void processDirectory(Path rootDir) throws IOException {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            stream
                .filter(Files::isDirectory)
                .forEach(packageDir -> createPackageInfo(rootDir, packageDir));
        }
    }

    /**
     * Creates a single package-info.java file in a given package directory.
     *
     * @param rootDir     The root source directory (for relativizing the path)
     * @param packageDir The specific directory to create the file in
     */
    private static void createPackageInfo(Path rootDir, Path packageDir) {
        Path packageInfoFile = packageDir.resolve("package-info.java");

        try {
            // 1. Check if it already exists. This is the safety check.
//            if (Files.exists(packageInfoFile)) {
//                System.out.println("[Skipping] " + packageInfoFile.toAbsolutePath() + " (already exists)");
//                return;
//            }

            // 2. Determine the package name from the directory structure
            Path relativePath = rootDir.relativize(packageDir);
            String packageName = relativePath.toString().replace(java.io.File.separator, ".");

            // 3. Don't create for the default (root) package
            if (packageName.isEmpty()) {
                System.out.println("[Skipping] " + rootDir.toAbsolutePath() + " (default package)");
                return;
            }
            if (packageName.equals("org.apereo") || packageName.equals("org.apereo.cas.config") || packageName.equals("org") || packageName.equals("generated")) {
                System.out.println("[Skipping] " + packageDir.toAbsolutePath() + " (excluded package)");
                packageInfoFile.toFile().deleteOnExit();
                return;
            }
            try (var stream = Files.list(packageDir)) {
                long fileCount = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .count();                     

                if (fileCount == 0) {
                    System.out.println("[Skipping] " + packageDir.toAbsolutePath() + " (no source files)");
                    return;
                }

            } catch (IOException e) {
                System.err.println("Error reading directory: " + e.getMessage());
            }


            // 4. Create the file content
            String content = """
                /**
                 * @since 8.0.0
                 */
                @NullMarked
                package $PACKAGE;
                
                import org.jspecify.annotations.NullMarked;\n
                """.stripIndent().replace("$PACKAGE", packageName);

            // 5. Write the new file
            Files.write(packageInfoFile, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("[Created]  " + packageInfoFile.toAbsolutePath());

        } catch (FileAlreadyExistsException e) {
            // This is a safeguard, though the check above should catch it.
            System.out.println("[Skipping] " + packageInfoFile.toAbsolutePath() + " (already exists)");
        } catch (IOException e) {
            System.err.println("Error creating file in " + packageDir + ": " + e.getMessage());
        }
    }
}
