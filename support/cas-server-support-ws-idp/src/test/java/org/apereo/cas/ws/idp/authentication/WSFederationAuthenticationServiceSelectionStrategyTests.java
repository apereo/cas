package org.apereo.cas.ws.idp.authentication;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class WSFederationAuthenticationServiceSelectionStrategyTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy;

    @Test
    public void verifySupports() {
        assertFalse(wsFederationAuthenticationServiceSelectionStrategy.supports(null));

        val service = RegisteredServiceTestUtils.getService("https://cas.com");
        assertFalse(wsFederationAuthenticationServiceSelectionStrategy.supports(service));

        val service1 = RegisteredServiceTestUtils.getService("https://cas.com?" + WSFederationConstants.WREPLY
            + "=wreply&" + WSFederationConstants.WTREALM + "=realm");
        assertTrue(wsFederationAuthenticationServiceSelectionStrategy.supports(service1));

        val service2 = RegisteredServiceTestUtils.getService("https://cas.com?" + WSFederationConstants.WREPLY + "=wreply");
        assertFalse(wsFederationAuthenticationServiceSelectionStrategy.supports(service2));

    }

}
