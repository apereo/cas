package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.YubiKeyAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.YubiKeyAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.YubiKeyAuthenticationWebflowConfiguration;
import org.apereo.cas.config.YubiKeyComponentSerializationConfiguration;
import org.apereo.cas.config.YubiKeyConfiguration;
import org.apereo.cas.config.YubiKeyRestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

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
        CasCoreServicesConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasThemesConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration("YubiKeyTestConfiguration")
    public static class YubiKeyTestConfiguration {
        private static final String BAD_TOKEN = "123456";

        @Bean
        public YubiKeyAccountValidator yubiKeyAccountValidator() {
            return (uid, token) -> !token.equals(BAD_TOKEN);
        }
    }
}
