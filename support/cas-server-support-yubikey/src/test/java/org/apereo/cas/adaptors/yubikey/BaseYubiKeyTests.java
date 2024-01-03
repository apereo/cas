package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasThemesConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.config.YubiKeyAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.YubiKeyAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.YubiKeyAuthenticationWebflowConfiguration;
import org.apereo.cas.config.YubiKeyComponentSerializationConfiguration;
import org.apereo.cas.config.YubiKeyConfiguration;
import org.apereo.cas.config.YubiKeyRestConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link BaseYubiKeyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseYubiKeyTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        YubiKeyTestConfiguration.class,
        MultifactorAuthnTrustConfiguration.class,
        MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
        MultifactorAuthnTrustWebflowConfiguration.class,
        YubiKeyAuthenticationWebflowConfiguration.YubiKeyMultifactorTrustConfiguration.class,
        YubiKeyAuthenticationEventExecutionPlanConfiguration.class,
        YubiKeyAuthenticationMultifactorProviderBypassConfiguration.class,
        YubiKeyComponentSerializationConfiguration.class,
        YubiKeyRestConfiguration.class,
        YubiKeyAuthenticationWebflowConfiguration.class,
        YubiKeyConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasThemesConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "YubiKeyTestConfiguration", proxyBeanMethods = false)
    public static class YubiKeyTestConfiguration {
        private static final String BAD_TOKEN = "123456";

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public YubiKeyAccountValidator yubiKeyAccountValidator() {
            return (uid, token) -> !token.equals(BAD_TOKEN);
        }
    }
}
