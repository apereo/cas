package org.apereo.cas.util.nativex;

import org.apereo.cas.util.ReflectionUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.DecoratingProxy;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link CasRuntimeHintsRegistrar}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface CasRuntimeHintsRegistrar extends RuntimeHintsRegistrar {

    /**
     * Holds the string that is the name of the system property providing information about the
     * context in which code is currently executing.
     */
    String PROPERTY_IMAGE_CODE_KEY = "org.graalvm.nativeimage.imagecode";

    /**
     * Holds the string that will be returned by the system property code is executing in the context of image
     * building (e.g. in a static initializer of class that will be contained in the image).
     *
     * @since 19.0
     */
    String PROPERTY_IMAGE_CODE_VALUE_BUILDTIME = "buildtime";

    /**
     * Holds the string that will be returned if code is executing at image runtime.
     */
    String PROPERTY_IMAGE_CODE_VALUE_RUNTIME = "runtime";

    /**
     * System property set to true during spring AOT processing phase.
     */
    String SYSTEM_PROPERTY_SPRING_AOT_PROCESSING = "spring.aot.processing";

    /**
     * Register spring proxies.
     *
     * @param hints the hints
     * @param clazz the clazz
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerSerializableSpringProxy(final RuntimeHints hints, final Class... clazz) {
        val proxies = Arrays.stream(clazz).collect(Collectors.toList());
        proxies.add(Serializable.class);
        addSpringProxyInterfaces(proxies);
        hints.proxies()
            .registerJdkProxy(clazz)
            .registerJdkProxy(proxies.toArray(ArrayUtils.EMPTY_CLASS_ARRAY));
        return this;
    }

    /**
     * Register spring proxy.
     *
     * @param hints the hints
     * @param clazz the clazz
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerSpringProxy(final RuntimeHints hints, final Class... clazz) {
        val proxies = Arrays.stream(clazz).collect(Collectors.toList());
        addSpringProxyInterfaces(proxies);
        hints.proxies()
            .registerJdkProxy(clazz)
            .registerJdkProxy(proxies.toArray(ArrayUtils.EMPTY_CLASS_ARRAY));
        return this;
    }

    /**
     * Register declared method as invokable.
     *
     * @param hints the hints
     * @param clazz the clazz
     * @param name  the name
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerDeclaredMethod(final RuntimeHints hints, final Class clazz,
                                                            final String name) {
        val method = Unchecked.supplier(() -> clazz.getDeclaredMethod(name)).get();
        hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
        return this;
    }

    /**
     * Find subclasses in packages and exclude tests.
     *
     * @param superClass the parent class
     * @param packages    the packages
     * @return the collection
     */
    default Collection<Class> findSubclassesInPackage(final Class superClass, final String... packages) {
        val results = (Collection<Class>) ReflectionUtils.findSubclassesInPackage(superClass, packages);
        val filteredResults = results
            .stream()
            .filter(clazz -> {
                var host = clazz.getCanonicalName();
                if (clazz.isMemberClass() && clazz.getPackageName().startsWith("org.apereo.cas")) {
                    var entry = clazz;
                    while (entry.isMemberClass()) {
                        entry = clazz.getNestHost();
                    }
                    host = entry.getCanonicalName();
                }
                return StringUtils.isNotBlank(host) && !host.endsWith("Tests");
            })
            .collect(Collectors.toList());
        filteredResults.add(superClass);
        return filteredResults;
    }

    /**
     * Determine if code is executing or being aot-rocessed in a GraalVM native image.
     *
     * @return true/false
     */
    static boolean inNativeImage() {
        return inImageBuildtimeCode()
            || inImageRuntimeCode()
            || BooleanUtils.toBoolean(System.getProperty(SYSTEM_PROPERTY_SPRING_AOT_PROCESSING));
    }

    /**
     * Determine if code is NOT executing or being aot-rocessed in a GraalVM native image.
     *
     * @return true/false
     */
    static boolean notInNativeImage() {
        return !inNativeImage();
    }

    /**
     * Returns true if (at the time of the call) code is executing at image runtime. This method
     * will be const-folded. It can be used to hide parts of an application that only work when
     * running as native image.
     *
     * @since 19.0
     */
    private static boolean inImageRuntimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_RUNTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }

    /**
     * Returns true if (at the time of the call) code is executing in the context of image building
     * (e.g. in a static initializer of class that will be contained in the image).
     */
    private static boolean inImageBuildtimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_BUILDTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }

    private static void addSpringProxyInterfaces(final List<Class> proxies) {
        proxies.add(SpringProxy.class);
        proxies.add(Advised.class);
        proxies.add(DecoratingProxy.class);
    }
}
