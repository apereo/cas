package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

import org.apache.catalina.util.ServerInfo;
import org.springframework.core.env.Environment;

import java.util.Formatter;

/**
 * This is {@link CasStarterBanner}.
 *
 * @author Hal Deadman
 * @since 6.5.0
 */
public class CasStarterBanner extends AbstractCasBanner {
    @Override
    protected void injectEnvironmentInfoIntoBanner(final Formatter formatter, final Environment environment,
                                                   final Class<?> sourceClass) {
        formatter.format("CAS Starter Apache Tomcat Version: %s%n", ServerInfo.getServerInfo());
        formatter.format("%s%n", LINE_SEPARATOR);
    }
}
