package org.apereo.cas;

import org.apache.catalina.util.ServerInfo;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.springframework.core.env.Environment;

import java.util.Formatter;

/**
 * This is {@link CasTomcatBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasTomcatBanner extends AbstractCasBanner {
    @Override
    protected void injectEnvironmentInfoIntoBanner(final Formatter formatter, final Environment environment,
                                                   final Class<?> sourceClass) {
        formatter.format("Apache Tomcat Version: %s%n", ServerInfo.getServerInfo());
        formatter.format("%s%n", LINE_SEPARATOR);
    }
}
