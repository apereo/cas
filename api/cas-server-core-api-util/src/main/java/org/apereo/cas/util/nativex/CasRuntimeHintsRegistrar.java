package org.apereo.cas.util.nativex;

import org.apereo.cas.util.ReflectionUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
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

    private static void addSpringProxyInterfaces(final List<Class> proxies) {
        proxies.add(SpringProxy.class);
        proxies.add(Advised.class);
        proxies.add(DecoratingProxy.class);
    }


    /**
     * Find subclasses in packages and exclude tests.
     *
     * @param parentClass the parent class
     * @param packages    the packages
     * @return the collection
     */
    default Collection<Class> findSubclassesInPackage(final Class parentClass, final String... packages) {
        val results = (Collection<Class>) ReflectionUtils.findSubclassesInPackage(parentClass, packages);
        return results
            .stream()
            .filter(clazz -> {
                var host = clazz.getCanonicalName();
                if (clazz.isMemberClass()) {
                    var entry = clazz;
                    while (entry.isMemberClass()) {
                        entry = clazz.getNestHost();
                    }
                    host = entry.getCanonicalName();
                }
                return StringUtils.isNotBlank(host) && !host.endsWith("Tests");
            })
            .collect(Collectors.toList());
    }
}
