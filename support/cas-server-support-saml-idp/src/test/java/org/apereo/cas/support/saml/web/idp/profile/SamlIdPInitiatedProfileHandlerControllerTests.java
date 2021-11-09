package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPInitiatedProfileHandlerControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlIdPInitiatedProfileHandlerControllerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("idpInitiatedSamlProfileHandlerController")
    private SamlIdPInitiatedProfileHandlerController idpInitiatedSamlProfileHandlerController;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        this.samlRegisteredService = getSamlRegisteredServiceForTestShib();
        this.samlRegisteredService.setSignUnsolicitedAuthnRequest(true);
        servicesManager.save(samlRegisteredService);
    }

    @Test
    public void verifyNoShire() {
        val request = new MockHttpServletRequest();

        val service = getSamlRegisteredServiceForTestShib();
        service.setServiceId("no:acs:service");
        servicesManager.save(service);

        request.addParameter(SamlIdPConstants.PROVIDER_ID, service.getServiceId());
        val response = new MockHttpServletResponse();
        assertThrows(MessageDecodingException.class,
            () -> idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request));
    }

    @Test
    public void verifyBadServiceWithNoMetadata() {
        val request = new MockHttpServletRequest();

        val service = new SamlRegisteredService();
        service.setServiceId(UUID.randomUUID().toString());
        servicesManager.save(service);

        request.addParameter(SamlIdPConstants.PROVIDER_ID, service.getServiceId());
        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedServiceException.class,
            () -> idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request));
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
        request.addParameter("CName1", "SomeParameter");
        request.addParameter("CName2", "SomeParameter");
        request.addParameter(SamlIdPConstants.TARGET, "relay-state");
        val response = new MockHttpServletResponse();
        val mv = idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request);
        assertEquals(HttpStatus.FOUND, mv.getStatus());
        val view = (RedirectView) mv.getView();
        assertTrue(view.getUrl().contains("CName1="));
        assertTrue(view.getUrl().contains("CName2="));
    }

    @Test
    @SuppressWarnings("JavaUtilDate")
    public void verifyOperationWithTime() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter(SamlIdPConstants.PROVIDER_ID, samlRegisteredService.getServiceId());
        request.addParameter(SamlIdPConstants.TARGET, "relay-state");
        request.addParameter(SamlIdPConstants.TIME, String.valueOf(new Date().getTime()));
        val response = new MockHttpServletResponse();
        val mv = idpInitiatedSamlProfileHandlerController.handleIdPInitiatedSsoRequest(response, request);
        assertEquals(HttpStatus.FOUND, mv.getStatus());
    }
}
