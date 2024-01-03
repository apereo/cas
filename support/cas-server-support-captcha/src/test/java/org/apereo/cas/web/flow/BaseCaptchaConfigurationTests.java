package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCaptchaConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasThemesConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseCaptchaConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseCaptchaConfigurationTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCaptchaConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasThemesConfiguration.class

    })
    static class SharedTestConfiguration {
    }
}
