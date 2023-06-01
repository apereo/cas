package org.apereo.cas.util.nativex;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.DecoratingProxy;

import java.io.Serializable;

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
    default CasRuntimeHintsRegistrar registerSpringProxy(final RuntimeHints hints, final Class clazz) {
        hints.proxies().registerJdkProxy(clazz);
        hints.proxies().registerJdkProxy(clazz, Serializable.class, SpringProxy.class, Advised.class, DecoratingProxy.class);
        return this;
    }
}
