package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.eclipse.jetty.server.Server;
import org.springframework.core.env.Environment;

import java.util.Formatter;
/**
 * This is {@link CasJettyBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasJettyBanner extends AbstractCasBanner {
    @Override
    protected void injectEnvironmentInfoIntoBanner(final Formatter formatter, final Environment environment, final Class<?> sourceClass) {
        formatter.format("Jetty Version: %s%n", Server.getVersion());
        formatter.format("%s%n", LINE_SEPARATOR);
    }
}
