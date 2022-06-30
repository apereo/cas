package org.apereo.cas.configuration.metadata;

import org.apereo.cas.util.ReflectionUtils;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.val;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link ConfigurationMetadataClassSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ConfigurationMetadataClassSourceLocator {

    private static ConfigurationMetadataClassSourceLocator INSTANCE;

    private final Map<String, Class> cachedPropertiesClasses = new HashMap<>(0);

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
        if (cachedPropertiesClasses.containsKey(type.getNameAsString())) {
            return cachedPropertiesClasses.get(type.getNameAsString());
        }

        val clz = ReflectionUtils.findClassBySimpleNameInPackage(type.getNameAsString(), "org.apereo.cas")
                .orElseThrow(() -> new IllegalArgumentException("Cant locate class for " + type.getNameAsString()));
        cachedPropertiesClasses.put(type.getNameAsString(), clz);
        return clz;
    }
}
