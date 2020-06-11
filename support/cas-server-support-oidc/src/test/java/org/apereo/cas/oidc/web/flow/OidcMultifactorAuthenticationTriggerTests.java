package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OidcMultifactorAuthenticationTriggerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcMultifactorAuthenticationTrigger")
    private MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger;
    
    @Test
    @Order(1)
    public void verifyNoAcr() {
        val service = RegisteredServiceTestUtils.getService();
        val request = new MockHttpServletRequest();
        val authn = RegisteredServiceTestUtils.getAuthentication();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        assertTrue(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request, service).isEmpty());
    }

    @Test
    @Order(2)
    public void verifyAcrMissingMfa() {
        val service = RegisteredServiceTestUtils.getService();
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.ACR_VALUES, TestMultifactorAuthenticationProvider.ID);
        val authn = RegisteredServiceTestUtils.getAuthentication();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        assertThrows(AuthenticationException.class,
            () -> oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request, service));
    }
    
    @Test
    @Order(3)
    public void verifyAcrMfa() {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        
        val service = RegisteredServiceTestUtils.getService();
        val request = new MockHttpServletRequest();
        request.setQueryString(String.format("%s=https://app.org?%s=mfa-dummy",
            CasProtocolConstants.PARAMETER_SERVICE, OAuth20Constants.ACR_VALUES));
        val authn = RegisteredServiceTestUtils.getAuthentication();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        assertFalse(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request, service).isEmpty());
    }
}
