package org.apereo.cas.ws.idp.metadata;

import module java.base;
import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WSFederationMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WSFederation")
class WSFederationMetadataControllerTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("wsFederationMetadataController")
    private WSFederationMetadataController wsFederationMetadataController;

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        wsFederationMetadataController.doGet(request, response);
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = mock(MockHttpServletResponse.class);
        doThrow(new RuntimeException()).when(response).setContentType(anyString());
        doCallRealMethod().when(response).sendError(anyInt());
        doCallRealMethod().when(response).getStatus();
        wsFederationMetadataController.doGet(request, response);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }
}
