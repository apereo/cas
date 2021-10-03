package org.apereo.cas.qr;

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
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.QRAuthenticationConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseQRAuthenticationTokenValidatorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public abstract class BaseQRAuthenticationTokenValidatorServiceTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        QRAuthenticationConfiguration.class,
        TokenCoreConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCookieConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreNotificationsConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
