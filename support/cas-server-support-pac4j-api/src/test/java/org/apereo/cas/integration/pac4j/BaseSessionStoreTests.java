package org.apereo.cas.integration.pac4j;

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
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseSessionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BaseSessionStoreTests {
    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreTicketsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
