package org.apereo.cas.authentication;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SecurityTokenServiceTokenFetcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasWsSecurityTokenTicketCatalogConfiguration.class,
    CoreWsSecuritySecurityTokenServiceConfiguration.class,
    CoreWsSecurityIdentityProviderConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.wsfedIdp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
    "cas.authn.wsfedIdp.idp.realmName=CAS",
    "cas.authn.wsfedIdp.sts.signingKeystoreFile=classpath:ststrust.jks",
    "cas.authn.wsfedIdp.sts.signingKeystorePassword=storepass",
    "cas.authn.wsfedIdp.sts.encryptionKeystoreFile=classpath:stsencrypt.jks",
    "cas.authn.wsfedIdp.sts.encryptionKeystorePassword=storepass",
    "cas.authn.wsfedIdp.sts.subjectNameIdFormat=unspecified",
    "cas.authn.wsfedIdp.sts.encryptTokens=true",
    "cas.authn.wsfedIdp.sts.realm.keystoreFile=classpath:stsrealm_a.jks",
    "cas.authn.wsfedIdp.sts.realm.keystorePassword=storepass",
    "cas.authn.wsfedIdp.sts.realm.keystoreAlias=realma",
    "cas.authn.wsfedIdp.sts.realm.keyPassword=realma",
    "cas.authn.wsfedIdp.sts.realm.issuer=CAS"
})
public class SecurityTokenServiceTokenFetcherTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("securityTokenServiceTokenFetcher")
    private SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher;

    @Test
    public void verifySecurityPopulator() {
        val realm = casProperties.getAuthn().getWsfedIdp().getIdp().getRealm();

        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm(realm);
        registeredService.setServiceId("http://app.example.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo(realm);
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);

        val service = CoreAuthenticationTestUtils.getService("http://example.org?"
            + WSFederationConstants.WREPLY + '=' + registeredService.getServiceId() + '&'
            + WSFederationConstants.WTREALM + '=' + realm);

        assertThrows(AuthenticationException.class, () -> securityTokenServiceTokenFetcher.fetch(service, "test"));
    }
}
