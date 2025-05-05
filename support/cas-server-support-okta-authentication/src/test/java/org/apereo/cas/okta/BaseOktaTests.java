package org.apereo.cas.okta;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseOktaTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BaseOktaTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import({CasRegisteredServicesTestConfiguration.class, CasAuthenticationEventExecutionPlanTestConfiguration.class})
    public static class SharedTestConfiguration {
    }
}
