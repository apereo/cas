package org.apereo.cas.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public abstract class BaseAutoConfigurationTests {
    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        ObservationAutoConfiguration.class,
    })
    @SpringBootConfiguration
    @Import({
        CasCoreMonitorAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
