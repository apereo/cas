package org.apereo.cas.okta;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseOktaTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BaseOktaTests {
    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
