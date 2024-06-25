package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
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
import org.apereo.cas.config.CasMultifactorAuthnTrustAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRadiusMultifactorAuthenticationAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseRadiusMultifactorAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseRadiusMultifactorAuthenticationTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,

        CasMultifactorAuthnTrustAutoConfiguration.class,
        CasRadiusMultifactorAuthenticationAutoConfiguration.RadiusMultifactorTrustConfiguration.class,
        CasRadiusMultifactorAuthenticationAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(CasPersonDirectoryTestConfiguration.class)
    public static class SharedTestConfiguration {
    }
}
