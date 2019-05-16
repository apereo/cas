package org.apereo.cas.support.wsfederation;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.wsfederation.config.WsFederationAuthenticationConfiguration;
import org.apereo.cas.support.wsfederation.config.support.authentication.WsFedAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Collection;

/**
 * Abstract class, provides resources to run wsfed tests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@SpringBootTest(classes = {
    AbstractOpenSamlTests.SamlTestConfiguration.class,
    WsFederationAuthenticationConfiguration.class,
    WsFedAuthenticationEventExecutionPlanConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CoreSamlConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreWebflowConfiguration.class,
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasValidationConfiguration.class,
    CasProtocolViewsConfiguration.class,
    CasCoreValidationConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.wsfed[0].identityProviderUrl=https://adfs.example.com/adfs/ls/",
    "cas.authn.wsfed[0].identityProviderIdentifier=http://adfs.example.com/adfs/services/trust",
    "cas.authn.wsfed[0].relyingPartyIdentifier=urn:federation:cas",
    "cas.authn.wsfed[0].attributesType=WSFED",
    "cas.authn.wsfed[0].signingCertificateResources=classpath:adfs-signing.crt",
    "cas.authn.wsfed[0].identityAttribute=upn",
    "cas.authn.wsfed[0].attributeResolverEnabled=true",
    "cas.authn.wsfed[0].autoRedirect=false",
    "cas.authn.wsfed[0].name=Test ADFS1"
})
@ContextConfiguration(locations = {"classpath:/applicationContext.xml"})
public class AbstractWsFederationTests extends AbstractOpenSamlTests {
    @Autowired
    @Qualifier("wsFederationConfigurations")
    protected Collection<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    @Qualifier("wsFederationCookieManager")
    protected WsFederationCookieManager wsFederationCookieManager;

    @Autowired
    @Qualifier("wsFederationHelper")
    protected WsFederationHelper wsFederationHelper;

}

