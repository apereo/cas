package org.apereo.cas;

import module java.base;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.eclipse.jetty.server.Server;
import org.springframework.core.env.Environment;

/**
 * This is {@link CasJettyBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasJettyBanner extends AbstractCasBanner {
    @Override
    public void injectEnvironmentInfo(final Formatter formatter, final Environment environment,
                                      final Class<?> sourceClass) {
        super.injectEnvironmentInfo(formatter, environment, sourceClass);
        formatter.format("Jetty Version: %s%n", Server.getVersion());
    }
}
