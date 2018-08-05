package org.apereo.cas.configuration.metadata;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link ConfigurationMetadataClassSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class ConfigurationMetadataClassSourceLocator {
    private static ConfigurationMetadataClassSourceLocator INSTANCE;

    private final Map<String, Class> cachedPropertiesClasses = new HashMap<>();

    public static ConfigurationMetadataClassSourceLocator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigurationMetadataClassSourceLocator();
        }
        return INSTANCE;
    }

    public static String buildTypeSourcePath(final String sourcePath, final String type) {
        val newName = type.replace(".", File.separator);
        return sourcePath + "/src/main/java/" + newName + ".java";
    }

    public Class locatePropertiesClassForType(final ClassOrInterfaceType type) {
        if (cachedPropertiesClasses.containsKey(type.getNameAsString())) {
            return cachedPropertiesClasses.get(type.getNameAsString());
        }

        final Predicate<String> filterInputs = s -> s.contains(type.getNameAsString());
        final Predicate<String> filterResults = s -> s.endsWith(type.getNameAsString());
        val packageName = ConfigurationMetadataGenerator.class.getPackage().getName();
        val reflections =
            new Reflections(new ConfigurationBuilder()
                .filterInputsBy(filterInputs)
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new TypeElementsScanner()
                        .includeFields(false)
                        .includeMethods(false)
                        .includeAnnotations(false)
                        .filterResultsBy(filterResults),
                    new SubTypesScanner(false)));
        val clz = reflections.getSubTypesOf(Serializable.class).stream()
            .filter(c -> c.getSimpleName().equalsIgnoreCase(type.getNameAsString()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cant locate class for " + type.getNameAsString()));
        cachedPropertiesClasses.put(type.getNameAsString(), clz);
        return clz;
    }
}
