package org.apereo.cas;

import module java.base;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.apache.catalina.util.ServerInfo;
import org.springframework.core.env.Environment;

/**
 * This is {@link CasTomcatBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasTomcatBanner extends AbstractCasBanner {
    @Override
    public void injectEnvironmentInfo(final Formatter formatter, final Environment environment,
                                      final Class<?> sourceClass) {
        super.injectEnvironmentInfo(formatter, environment, sourceClass);
        formatter.format("Apache Tomcat Version: %s%n", ServerInfo.getServerInfo());
    }
}
