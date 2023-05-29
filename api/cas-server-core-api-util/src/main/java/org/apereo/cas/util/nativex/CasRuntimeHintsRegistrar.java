package org.apereo.cas.util.nativex;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.ExecutableMode;
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
        hints.proxies()
            .registerJdkProxy(clazz)
            .registerJdkProxy(clazz, Serializable.class, SpringProxy.class, Advised.class, DecoratingProxy.class);
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
}
