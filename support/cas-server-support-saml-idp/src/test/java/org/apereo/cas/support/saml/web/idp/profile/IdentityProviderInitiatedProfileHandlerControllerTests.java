package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link IdentityProviderInitiatedProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class IdentityProviderInitiatedProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("idpInitiatedSamlProfileHandlerController")
    private IdentityProviderInitiatedProfileHandlerController idpInitiatedSamlProfileHandlerController;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        this.samlRegisteredService = getSamlRegisteredServiceForTestShib();
        this.samlRegisteredService.setSignUnsolicitedAuthnRequest(true);
        servicesManager.save(samlRegisteredService);
    }

    @Test
    public void verifyNoProvider() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertThrows(MessageDecodingException.class,
            () -> idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request));
    }

    @Test
    public void verifyBadService() {
        val request = new MockHttpServletRequest();
        request.addParameter(SamlIdPConstants.PROVIDER_ID, "xxxxxx");
        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedServiceException.class,
            () -> idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request));
    }

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter(SamlIdPConstants.PROVIDER_ID, samlRegisteredService.getServiceId());
        request.addParameter(SamlIdPConstants.TARGET, "relay-state");
        val response = new MockHttpServletResponse();
        idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
    }

    @Test
    public void verifyOperationWithTime() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter(SamlIdPConstants.PROVIDER_ID, samlRegisteredService.getServiceId());
        request.addParameter(SamlIdPConstants.TARGET, "relay-state");
        request.addParameter(SamlIdPConstants.TIME, "100_000");
        val response = new MockHttpServletResponse();
        idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
    }
}
