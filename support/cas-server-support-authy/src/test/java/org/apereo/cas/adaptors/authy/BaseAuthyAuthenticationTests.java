package org.apereo.cas.adaptors.authy;

import org.apereo.cas.config.AuthyAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.config.AuthyAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.AuthyAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.AuthyConfiguration;
import org.apereo.cas.config.AuthyConfiguration.AuthyMultifactorTrustConfiguration;
import org.apereo.cas.config.CasCookieConfiguration;
import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCoreWebflowConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseAuthyAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseAuthyAuthenticationTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuditConfiguration.class,

        MultifactorAuthnTrustConfiguration.class,
        MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
        MultifactorAuthnTrustWebflowConfiguration.class,
        AuthyMultifactorTrustConfiguration.class,

        AuthyConfiguration.class,
        AuthyAuthenticationEventExecutionPlanConfiguration.class,
        AuthyAuthenticationComponentSerializationConfiguration.class,
        AuthyAuthenticationMultifactorProviderBypassConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
