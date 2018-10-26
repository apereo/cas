package org.apereo.cas.support.openid;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.OpenIdConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.OpenIdAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.support.authentication.OpenIdServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.OpenIdUniqueTicketIdGeneratorConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;

import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * Bootstrap context for openid tests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@SpringBootTest(classes = {
    CasCoreServicesConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasProtocolViewsConfiguration.class,
    CasCookieConfiguration.class,
    CasValidationConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreValidationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreUtilConfiguration.class,
    OpenIdConfiguration.class,
    OpenIdUniqueTicketIdGeneratorConfiguration.class,
    OpenIdServiceFactoryConfiguration.class,
    OpenIdAuthenticationEventExecutionPlanConfiguration.class,
    ThymeleafAutoConfiguration.class
})
public abstract class AbstractOpenIdTests {
    @Autowired
    @Qualifier("serverManager")
    protected ServerManager serverManager;

    @Autowired
    @Qualifier("openIdServiceFactory")
    protected OpenIdServiceFactory openIdServiceFactory;

    @Autowired
    @Qualifier("centralAuthenticationService")
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    protected AuthenticationSystemSupport authenticationSystemSupport;

    public OpenIdServiceFactory getOpenIdServiceFactory() {
        return openIdServiceFactory;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return authenticationSystemSupport;
    }
}
