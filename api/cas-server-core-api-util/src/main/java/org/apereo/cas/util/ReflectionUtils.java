package org.apereo.cas.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.experimental.UtilityClass;
import lombok.val;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reflection Utilities based on {@link ClassGraph}.
 *
 * @author Lars Grefer
 * @since 6.6.0
 */
@UtilityClass
public class ReflectionUtils {

    /**
     * Finds all {@link Class classes} extending or implementing the given superclass below the given package.
     *
     * @param superclass  The superclass to look for subclasses or implementors.
     * @param packageName The base package to look in.
     * @param <T>         The type of the superclass/interface.
     * @return The - possibly empty - collection of subclasses.
     */
    public <T> Collection<Class<? extends T>> findSubclassesInPackage(final Class<T> superclass, final String... packageName) {
        try (val scanResult = new ClassGraph()
            .acceptPackages(packageName)
            .enableClassInfo()
            .enableInterClassDependencies()
            .ignoreClassVisibility()
            .enableSystemJarsAndModules()
            .removeTemporaryFilesAfterScan()
            .enableAnnotationInfo()
            .scan()) {
            return superclass.isInterface()
                ? new ArrayList<>(scanResult.getClassesImplementing(superclass).loadClasses(superclass))
                : new ArrayList<>(scanResult.getSubclasses(superclass).loadClasses(superclass));
        }
    }

    /**
     * Finds all classes in the given package, which are annotated with at least one of the given annotations.
     *
     * @param annotations The annotations to look for.
     * @param packageName The base package to look in.
     * @return The - possibly empty - collection of annotated classes.
     */
    public Collection<Class<?>> findClassesWithAnnotationsInPackage(final Collection<Class<? extends Annotation>> annotations,
                                                                    final String... packageName) {
        try (val scanResult = new ClassGraph()
            .acceptPackages(packageName)
            .enableAnnotationInfo()
            .scan()) {
            return annotations
                .stream()
                .map(annotation -> scanResult.getClassesWithAnnotation(annotation).loadClasses())
                .flatMap(List::stream)
                .collect(Collectors.toList());
        }
    }

    /**
     * Finds a class with the given {@link Class#getSimpleName()} below the given package.
     *
     * @param simpleName  The simple name of the class.
     * @param packageName The base package to look in.
     * @return The found class.
     */
    public static Optional<Class<?>> findClassBySimpleNameInPackage(final String simpleName, final String... packageName) {
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
