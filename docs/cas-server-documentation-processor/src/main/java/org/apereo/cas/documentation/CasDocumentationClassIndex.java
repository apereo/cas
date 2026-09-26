package org.apereo.cas.documentation;

import module java.base;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.annotation.Annotation;

/**
 * One classpath scan shared by every exporter, answering the lookups that
 * {@link org.apereo.cas.util.ReflectionUtils} would otherwise answer with a full scan each.
 */
final class CasDocumentationClassIndex implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasDocumentationClassIndex.class);

    private final ScanResult scanResult;

    CasDocumentationClassIndex(final String packageName) {
        this.scanResult = new ClassGraph()
            .acceptPackages(packageName)
            .enableClassInfo()
            .enableAnnotationInfo()
            .ignoreClassVisibility()
            .removeTemporaryFilesAfterScan()
            .disableModuleScanning()
            .scan();
    }

    <T> List<Class<? extends T>> findSubclassesInPackage(final Class<T> superclass, final String packageName) {
        var candidates = superclass.isInterface()
            ? scanResult.getClassesImplementing(superclass)
            : scanResult.getSubclasses(superclass);
        return new ArrayList<>(inPackage(candidates, packageName).loadClasses(superclass));
    }

    List<Class<?>> findClassesWithAnnotationsInPackage(final Collection<Class<? extends Annotation>> annotations,
                                                       final String packageName) {
        return annotations
            .stream()
            .map(annotation -> inPackage(scanResult.getClassesWithAnnotation(annotation), packageName).getNames())
            .flatMap(List::stream)
            .flatMap(name -> loadClass(name).stream())
            .collect(Collectors.toList());
    }

    @Override
    public void close() {
        scanResult.close();
    }

    private static ClassInfoList inPackage(final ClassInfoList classes, final String packageName) {
        return classes.filter(info -> info.getPackageName().equals(packageName)
            || info.getPackageName().startsWith(packageName + '.'));
    }

    private static Optional<Class<?>> loadClass(final String name) {
        try {
            return Optional.of(ClassUtils.getClass(name));
        } catch (final Exception e) {
            LOGGER.debug("Unable to load class [{}]", name, e);
            return Optional.empty();
        }
    }
}
