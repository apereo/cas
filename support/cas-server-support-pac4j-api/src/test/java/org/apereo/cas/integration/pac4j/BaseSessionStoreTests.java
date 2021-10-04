package org.apereo.cas.integration.pac4j;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
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
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreTicketComponentSerializationConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreWebConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreUtilConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
