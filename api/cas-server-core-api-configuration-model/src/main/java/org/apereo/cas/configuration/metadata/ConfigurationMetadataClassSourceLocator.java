package org.apereo.cas.configuration.metadata;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.val;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link ConfigurationMetadataClassSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ConfigurationMetadataClassSourceLocator {

    private static final Pattern GENERIC_TYPED_CLASS = Pattern.compile("\\w+<(\\w+)>");

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
        val newName = type.replace(".", File.separator);
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

    static Optional<Class<?>> findClassBySimpleNameInPackage(final String simpleName, final String... packageName) {
        try (val scanResult = new ClassGraph()
            .acceptPackages(packageName)
            .enableClassInfo()
            .scan()) {

            return scanResult.getAllClasses()
                .stream()
                .filter(classInfo -> classInfo.getSimpleName().equalsIgnoreCase(simpleName))
                .findFirst()
                .map(ClassInfo::loadClass);
        }
    }
}
