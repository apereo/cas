package org.apereo.cas.mfa.simple;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.push.NotificationSenderExecutionPlanConfigurer;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseCasSimpleMultifactorAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseCasSimpleMultifactorAuthenticationTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        MultifactorAuthnTrustConfiguration.class,
        MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
        MultifactorAuthnTrustWebflowConfiguration.class,
        
        CasSimpleMultifactorAuthenticationAutoConfiguration.CasSimpleMultifactorTrustConfiguration.class,
        CasSimpleMultifactorAuthenticationAutoConfiguration.class,

        CasCoreLogoutAutoConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "CasSimpleMultifactorTestConfiguration", proxyBeanMethods = false)
    public static class CasSimpleMultifactorTestConfiguration implements NotificationSenderExecutionPlanConfigurer {
        @Bean
        public SmsSender smsSender() {
            return MockSmsSender.INSTANCE;
        }

        @Override
        public NotificationSender configureNotificationSender() {
            return NotificationSender.noOp();
        }
    }
}
