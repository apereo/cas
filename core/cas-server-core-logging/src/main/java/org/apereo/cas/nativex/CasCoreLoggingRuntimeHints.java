package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * This is {@link CasCoreLoggingRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class CasCoreLoggingRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        val entries = List.<Class>of(
            AsyncLoggerContextSelector.class,
            RuntimeMXBean.class
        );
        registerReflectionHints(hints, entries);
        registerSpringProxyHints(hints, InitializingBean.class, View.class,
            BeanNameAware.class, ServletContextAware.class, ApplicationContextAware.class);
    }
}

