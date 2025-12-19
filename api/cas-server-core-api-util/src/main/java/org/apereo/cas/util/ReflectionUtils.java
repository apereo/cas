package org.apereo.cas.util;

import module java.base;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.jooq.lambda.Unchecked;
import java.lang.annotation.Annotation;

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
     * @param superclass  The superclass to look for subclasses or implementers.
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
            .disableModuleScanning()
            .scan()) {
            return superclass.isInterface()
                ? new ArrayList<>(scanResult.getClassesImplementing(superclass).loadClasses(superclass))
                : new ArrayList<>(scanResult.getSubclasses(superclass).loadClasses(superclass));
        }
    }

    /**
     * Find classes with annotations in package collection.
     *
     * @param classLoaders the class loaders
     * @param annotations  the annotations
     * @param packageName  the package name
     * @return the collection
     */
    public Collection<Class<?>> findClassesWithAnnotationsInPackage(
        final List<ClassLoader> classLoaders,
        final Collection<Class<? extends Annotation>> annotations,
        final String... packageName) {
        
        var classGraph = new ClassGraph()
            .acceptPackages(packageName)
            .enableAnnotationInfo()
            .disableModuleScanning();
        if (!classLoaders.isEmpty()) {
            classGraph = classGraph.overrideClassLoaders(classLoaders.toArray(new ClassLoader[0]));
        }
        try (val scanResult = classGraph.scan()) {
            return annotations
                .stream()
                .map(annotation -> scanResult.getClassesWithAnnotation(annotation).getNames())
                .flatMap(List::stream)
                .map(Unchecked.function(ClassUtils::getClass))
                .collect(Collectors.toList());
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
        return findClassesWithAnnotationsInPackage(List.of(Thread.currentThread().getContextClassLoader()), annotations, packageName);
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
            .disableModuleScanning()
            .scan()) {

            return scanResult.getAllClasses()
                .stream()
                .filter(classInfo -> classInfo.getSimpleName().equalsIgnoreCase(simpleName))
                .findFirst()
                .map(ClassInfo::loadClass);
        }
    }
}
