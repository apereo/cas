package org.apereo.cas;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasGoogleAnalyticsConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseCasGoogleAnalyticsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseCasGoogleAnalyticsTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasGoogleAnalyticsConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
