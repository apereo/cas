package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * This is {@link BaseAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public abstract class BaseAutoConfigurationTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreMonitorAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }
}
