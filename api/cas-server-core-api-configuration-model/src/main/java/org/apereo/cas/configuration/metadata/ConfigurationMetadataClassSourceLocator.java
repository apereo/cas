package org.apereo.cas.configuration.metadata;

import module java.base;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.val;

/**
 * This is {@link ConfigurationMetadataClassSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ConfigurationMetadataClassSourceLocator {

    private static final Pattern GENERIC_TYPED_CLASS = Pattern.compile("\\w+<(\\w+)>");

    private static final Map<List<String>, Map<String, ClassInfo>> CLASSES_BY_SIMPLE_NAME = new ConcurrentHashMap<>();

    private static ConfigurationMetadataClassSourceLocator INSTANCE;

    private final Map<String, Class> cachedPropertiesClasses = new HashMap<>();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ConfigurationMetadataClassSourceLocator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigurationMetadataClassSourceLocator();
        }
        return INSTANCE;
    }

    /**
     * Build type source path string.
     *
     * @param sourcePath the source path
     * @param type       the type
     * @return the string
     */
    public static String buildTypeSourcePath(final String sourcePath, final String type) {
        val sourceType = type.contains("$") ? type.substring(0, type.indexOf('$')) : type;
        val newName = sourceType.replace(".", File.separator);
        return sourcePath + "/src/main/java/" + newName + ".java";
    }

    /**
     * Locate properties class for type class.
     *
     * @param type the type
     * @return the class
     */
    public Class locatePropertiesClassForType(final ClassOrInterfaceType type) {
        var typeName = type.getNameAsString();
        if (cachedPropertiesClasses.containsKey(typeName)) {
            return cachedPropertiesClasses.get(typeName);
        }

        val matcher = GENERIC_TYPED_CLASS.matcher(type.toString());
        if (matcher.matches()) {
            typeName = matcher.group(1);
        }

        val error = new IllegalArgumentException("Can't locate class for " + typeName);
        val clz = findClassBySimpleNameInPackage(typeName, "org.apereo.cas").orElseThrow(() -> error);
        cachedPropertiesClasses.put(typeName, clz);
        return clz;
    }

    /**
     * Find the first class, in name order, whose simple name matches ignoring case.
     * Each package list is scanned once: the generator looks up hundreds of names
     * (primitives such as {@code boolean} included), and a scan per lookup dominated its run time.
     *
     * @param simpleName  the simple name
     * @param packageName the packages to look in
     * @return the class, if any
     */
    static Optional<Class<?>> findClassBySimpleNameInPackage(final String simpleName, final String... packageName) {
        val index = CLASSES_BY_SIMPLE_NAME.computeIfAbsent(List.of(packageName),
            ConfigurationMetadataClassSourceLocator::indexClassesBySimpleName);
        return Optional.ofNullable(index.get(simpleName.toLowerCase(Locale.ROOT))).map(ClassInfo::loadClass);
    }

    private static Map<String, ClassInfo> indexClassesBySimpleName(final List<String> packageNames) {
        val scanResult = new ClassGraph()
            .acceptPackages(packageNames.toArray(String[]::new))
            .enableClassInfo()
            .scan();
        val index = new HashMap<String, ClassInfo>();
        scanResult.getAllClasses().forEach(classInfo -> index.putIfAbsent(classInfo.getSimpleName().toLowerCase(Locale.ROOT), classInfo));
        return index;
    }
}
