package org.apereo.cas.util.nativex;

import module java.base;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Initializes native application contexts through module-provided {@link ServiceLoader} implementations.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface CasNativeApplicationContextInitializer extends ApplicationContextInitializer<ConfigurableApplicationContext> {
}
