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
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.DecoratingProxy;
import org.springframework.util.ClassUtils;
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
    default CasRuntimeHintsRegistrar registerSerializableSpringProxyHints(final RuntimeHints hints, final Class... clazz) {
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
    default CasRuntimeHintsRegistrar registerSpringProxyHints(final RuntimeHints hints, final Class... clazz) {
        val proxies = Arrays.stream(clazz).collect(Collectors.toList());
        addSpringProxyInterfaces(proxies);
        hints.proxies()
            .registerJdkProxy(clazz)
            .registerJdkProxy(proxies.toArray(ArrayUtils.EMPTY_CLASS_ARRAY));
        return this;
    }

    /**
     * Register proxy hints together.
     *
     * @param hints               the hints
     * @param subclassesInPackage the subclasses in package
     */
    default void registerChainedProxyHints(final RuntimeHints hints, final Class... subclassesInPackage) {
        hints.proxies().registerJdkProxy(subclassesInPackage);
    }

    /**
     * Register proxy hints.
     *
     * @param hints               the hints
     * @param subclassesInPackage the subclasses in package
     */
    default void registerProxyHints(final RuntimeHints hints, final Class... subclassesInPackage) {
        Arrays.stream(subclassesInPackage).forEach(clazz -> hints.proxies().registerJdkProxy(clazz));
    }

    /**
     * Register proxy hints.
     *
     * @param hints               the hints
     * @param subclassesInPackage the subclasses in package
     */
    default void registerProxyHints(final RuntimeHints hints, final Collection<Class> subclassesInPackage) {
        subclassesInPackage.forEach(clazz -> hints.proxies().registerJdkProxy(clazz));
    }

    /**
     * Register serialization hints.
     *
     * @param hints   the hints
     * @param entries the entries
     */
    default void registerSerializationHints(final RuntimeHints hints, final Collection<Class> entries) {
        entries.forEach(el -> hints.serialization().registerType(el));
    }

    /**
     * Register serialization hints.
     *
     * @param hints   the hints
     * @param entries the entries
     */
    default void registerSerializationHints(final RuntimeHints hints, final Object... entries) {
        Arrays.stream(entries).forEach(el -> {
            if (el instanceof final TypeReference tr) {
                hints.serialization().registerType(tr);
            }
            if (el instanceof final Class clazz) {
                hints.serialization().registerType(clazz);
            }
        });
    }

    /**
     * Register declared method as invocable.
     *
     * @param hints the hints
     * @param clazz the clazz
     * @param name  the name
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintForDeclaredMethod(final RuntimeHints hints, final Class clazz,
                                                                             final String name) {
        val method = Unchecked.supplier(() -> clazz.getDeclaredMethod(name)).get();
        hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
        return this;
    }

    /**
     * Register reflection hints for types.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintsForTypes(final RuntimeHints hints, final Collection entries) {
        registerReflectionHints(hints, List.of(entries), new MemberCategory[0]);
        return this;
    }

    /**
     * Register reflection hints.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHints(final RuntimeHints hints, final Class... entries) {
        registerReflectionHints(hints, List.of(entries));
        return this;
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries,
                                                final MemberCategory... memberCategories) {
        entries.forEach(el -> {
            if (el instanceof final String clazz) {
                hints.reflection().registerType(TypeReference.of(clazz), memberCategories);
            }
            if (el instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
            if (el instanceof final TypeReference reference) {
                hints.reflection().registerType(reference, memberCategories);
            }
        });
    }

    /**
     * Register reflection hints.
     *
     * @param hints   the hints
     * @param entries the entries
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,

            MemberCategory.INTROSPECT_DECLARED_METHODS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS,

            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,

            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,

            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS
        };
        registerReflectionHints(hints, entries, memberCategories);
        return this;
    }

    /**
     * Register reflection hints for methods and fields.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintsForMethodsAndFields(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.PUBLIC_FIELDS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS
        };
        registerReflectionHints(hints, entries, memberCategories);
        return this;
    }

    /**
     * Register reflection hints for introspected public elements.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintsForIntrospectedPublicElements(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS
        };
        registerReflectionHints(hints, entries, memberCategories);
        return this;
    }

    /**
     * Register reflection hints for declared elements.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintsForDeclaredElements(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
        };
        registerReflectionHints(hints, entries, memberCategories);
        return this;
    }

    /**
     * Register reflection hints for constructors.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintsForConstructors(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,

            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
        };
        registerReflectionHints(hints, entries, memberCategories);
        return this;
    }

    /**
     * Register reflection hints for declared and public elements.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintsForDeclaredAndPublicElements(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS
        };
        registerReflectionHints(hints, entries, memberCategories);
        return this;
    }

    /**
     * Register reflection hints for public elements.
     *
     * @param hints   the hints
     * @param entries the entries
     * @return the cas runtime hints registrar
     */
    @CanIgnoreReturnValue
    default CasRuntimeHintsRegistrar registerReflectionHintsForPublicElements(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.PUBLIC_FIELDS
        };
        registerReflectionHints(hints, entries, memberCategories);
        return this;
    }

    /**
     * Find subclasses of class.
     *
     * @param superClass the super class
     * @return the collection
     */
    default Collection<Class> findSubclassesOf(final Class superClass) {
        return findSubclassesInPackage(superClass, "org.apereo.cas");
    }

    /**
     * Find subclasses in packages and exclude tests.
     *
     * @param superClass the parent class
     * @param packages   the packages
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
     * Is type present?
     *
     * @param classLoader the class loader
     * @param typeName    the type name
     * @return true/false
     */
    default boolean isTypePresent(final ClassLoader classLoader, final String typeName) {
        return ClassUtils.isPresent(typeName, classLoader);
    }

    /**
     * Is groovy present?
     *
     * @param classLoader the class loader
     * @return true/false
     */
    default boolean isGroovyPresent(final ClassLoader classLoader) {
        return isTypePresent(classLoader, "groovy.lang.GroovyObject");
    }

    /**
     * Determine if code is executing or being aot-processed in a GraalVM native image.
     *
     * @return true/false
     */
    static boolean inNativeImage() {
        return inImageBuildTimeCode()
            || inImageRuntimeCode()
            || BooleanUtils.toBoolean(System.getProperty(SYSTEM_PROPERTY_SPRING_AOT_PROCESSING));
    }

    /**
     * Determine if code is NOT executing or being aot-processed in a GraalVM native image.
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
    private static boolean inImageBuildTimeCode() {
        return PROPERTY_IMAGE_CODE_VALUE_BUILDTIME.equals(System.getProperty(PROPERTY_IMAGE_CODE_KEY));
    }

    private static void addSpringProxyInterfaces(final List<Class> proxies) {
        proxies.add(SpringProxy.class);
        proxies.add(Advised.class);
        proxies.add(DecoratingProxy.class);
    }

    /**
     * Log messages via System output stream.
     *
     * @param message the message
     */
    default void log(final String message) {
        //CHECKSTYLE:OFF
        System.out.println(message);
        //CHECKSTYLE:ON
    }
}
