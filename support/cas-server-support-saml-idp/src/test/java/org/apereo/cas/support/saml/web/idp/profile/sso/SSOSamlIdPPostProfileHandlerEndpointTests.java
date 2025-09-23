package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
@Tag("SAML2Web")
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.samlPostProfileResponse.access=UNRESTRICTED"
})
class SSOSamlIdPPostProfileHandlerEndpointTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("ssoSamlPostProfileHandlerEndpoint")
    private SSOSamlIdPPostProfileHandlerEndpoint endpoint;

    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        this.samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyPostOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val samlRequest = new SSOSamlIdPPostProfileHandlerEndpoint.SamlRequest("casuser",
            "casuser", samlRegisteredService.getServiceId(), false);
        val entity = endpoint.producePost(request, response, samlRequest);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    void verifyPostLogoutOperation() throws Throwable {
        val response = new MockHttpServletResponse();
        val entity = endpoint.produceLogoutRequestPost(samlRegisteredService.getServiceId(), response);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    void verifyPostOperationWithoutPassword() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val samlRequest = new SSOSamlIdPPostProfileHandlerEndpoint.SamlRequest("casuser",
            StringUtils.EMPTY, samlRegisteredService.getServiceId(), false);
        val entity = endpoint.producePost(request, response, samlRequest);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    void verifyBadCredentials() {
        val request = new MockHttpServletRequest();
        val samlRequest = new SSOSamlIdPPostProfileHandlerEndpoint.SamlRequest("xyz",
            "123", samlRegisteredService.getServiceId(), false);
        val response = new MockHttpServletResponse();
        val entity = endpoint.producePost(request, response, samlRequest);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    void verifyMissingEntity() {
        val request = new MockHttpServletRequest();
        val samlRequest = new SSOSamlIdPPostProfileHandlerEndpoint.SamlRequest("xyz",
            "123", null, false);
        val response = new MockHttpServletResponse();
        val entity = endpoint.producePost(request, response, samlRequest);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }
}
