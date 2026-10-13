package org.apereo.cas.util;

import module java.base;
import io.github.classgraph.ClassGraph;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import java.lang.annotation.Annotation;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * Reflection Utilities based on {@link ClassGraph}.
 *
 * @author Lars Grefer
 * @since 6.6.0
 */
@UtilityClass
@Slf4j
public class ReflectionUtils {

    private static final ClassLoader[] EMPTY_CLASSLOADERS = new ClassLoader[0];

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
            .enableClasspath()
            .enableSystemJars()
            .enableAnnotationInfo()
            .scan()) {
            val candidates = superclass.isInterface()
                ? scanResult.getAllClassesImplementing(superclass)
                : scanResult.getAllSubclasses(superclass);
            return candidates
                .stream()
                .map(info -> resolveClassName(info.getName(), Thread.currentThread().getContextClassLoader()))
                .map(clazz -> clazz.asSubclass(superclass))
                .collect(Collectors.toCollection(ArrayList::new));
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
            .enableClassInfo()
            .enableAnnotationInfo()
            .ignoreClassVisibility();
        if (classLoaders.isEmpty()) {
            classGraph.enableClasspath();
        } else {
            classGraph.enableClassLoaders(classLoaders.toArray(EMPTY_CLASSLOADERS));
        }

        try (val scanResult = classGraph.scan()) {
            return annotations
                .stream()
                .map(annotation -> scanResult.getClassesWithAnnotation(annotation).getNames())
                .flatMap(List::stream)
                .map(name -> {
                    try {
                        return ClassUtils.getClass(name);
                    } catch (final Exception e) {
                        LOGGER.debug("Unable to load class [{}]", name, e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
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
            .enableClasspath()
            .scan()) {

            return scanResult
                .getAllClasses()
                .stream()
                .filter(classInfo -> classInfo.getSimpleName().equalsIgnoreCase(simpleName))
                .findFirst()
                .map(info -> resolveClassName(info.getName(), Thread.currentThread().getContextClassLoader()));
        }
    }
}
