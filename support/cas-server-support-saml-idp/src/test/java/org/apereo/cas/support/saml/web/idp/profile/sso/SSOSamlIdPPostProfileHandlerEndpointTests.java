package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SSOSamlIdPPostProfileHandlerEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.samlPostProfileResponse.enabled=true"
})
public class SSOSamlIdPPostProfileHandlerEndpointTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ssoSamlPostProfileHandlerEndpoint")
    private SSOSamlIdPPostProfileHandlerEndpoint endpoint;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    public void beforeEach() {
        this.samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        servicesManager.save(samlRegisteredService);
    }

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addParameter("username", "casuser");
        request.addParameter("password", "casuser");
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, samlRegisteredService.getServiceId());
        val response = new MockHttpServletResponse();
        val entity = endpoint.produce(request, response);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    public void verifyBadCredentials() {
        val request = new MockHttpServletRequest();
        request.addParameter("username", "xyz");
        request.addParameter("password", "123");
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, samlRegisteredService.getServiceId());
        val response = new MockHttpServletResponse();
        val entity = endpoint.produce(request, response);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    public void verifyMissingEntity() {
        val request = new MockHttpServletRequest();
        request.addParameter("username", "xyz");
        request.addParameter("password", "123");
        val response = new MockHttpServletResponse();
        val entity = endpoint.produce(request, response);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }
}
