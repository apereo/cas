package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPushedAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.discovery.require-pushed-authorization-requests=true")
public class OidcPushedAuthorizeEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcPushedAuthorizeController")
    protected OidcPushedAuthorizeEndpointController oidcPushedAuthorizeController;

    @Test
    public void verifyGetOperation() throws Exception {
        assertNotNull(oidcPushedAuthorizeController);
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        val response = new MockHttpServletResponse();
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, oidcPushedAuthorizeController.handleRequest(request, response).getStatus());

        request.setMethod(HttpMethod.POST.name());
        assertEquals(HttpStatus.NOT_FOUND, oidcPushedAuthorizeController.handleRequestPost(request, response).getStatus());
    }
}
