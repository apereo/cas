package org.apereo.cas.ws.idp.metadata;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WSFederationMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WSFederation")
public class WSFederationMetadataControllerTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("wsFederationMetadataController")
    private WSFederationMetadataController wsFederationMetadataController;

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        wsFederationMetadataController.doGet(request, response);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }
}
