package org.apereo.cas.authentication;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.val;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SecurityTokenServiceClientBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class SecurityTokenServiceClientBuilderTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("securityTokenServiceClientBuilder")
    private SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @BeforeEach
    public void beforeEach() {
        servicesManager.deleteAll();
    }
    
    @Test
    public void verifyClientSecurityRequest() {
        val registeredService = getWsFederationRegisteredService();
        assertNotNull(securityTokenServiceClientBuilder.buildClientForSecurityTokenRequests(registeredService));
    }

    @Test
    public void verifyRelyingPartyTokenResponses() {
        val registeredService = getWsFederationRegisteredService();
        val token = new SecurityToken(UUID.randomUUID().toString());
        
        assertNotNull(securityTokenServiceClientBuilder.buildClientForRelyingPartyTokenResponses(token, registeredService));
    }

    private WSFederationRegisteredService getWsFederationRegisteredService() {
        val realm = casProperties.getAuthn().getWsfedIdp().getIdp().getRealm();
        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm(realm);
        registeredService.setServiceId("http://app.example.org/wsfed-idp");
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setPolicyNamespace("policy-namespace");
        registeredService.setAppliesTo(realm);
        servicesManager.save(registeredService);
        return registeredService;
    }

}
