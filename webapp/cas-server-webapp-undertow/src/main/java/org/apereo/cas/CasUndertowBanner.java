package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

import io.undertow.Version;
import org.springframework.core.env.Environment;

import java.util.Formatter;

/**
 * This is {@link CasUndertowBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasUndertowBanner extends AbstractCasBanner {
    @Override
    public void injectEnvironmentInfo(final Formatter formatter, final Environment environment,
                                      final Class<?> sourceClass) {
        formatter.format("Undertow Version: %s%n", Version.getFullVersionString());
    }
}
