package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcInitialAccessTokenControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OIDCWeb")
class OidcInitialAccessTokenControllerTests {

    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-mode=OPEN")
    @Nested
    class OpenRegistrationTests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcInitialAccessTokenController")
        protected OidcInitialAccessTokenController controller;

        @Test
        void verifyNotAllowed() {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.NOT_ACCEPTABLE, entity.getStatus());
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.oidc.registration.dynamic-client-registration-mode=PROTECTED",
        "cas.authn.oidc.registration.initial-access-token-user=casuser",
        "cas.authn.oidc.registration.initial-access-token-password=Mellon"
    })
    @Nested
    class ProtectedRegistrationTests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcInitialAccessTokenController")
        protected OidcInitialAccessTokenController controller;

        @Test
        void verifyMismatchedEndpoint() {
            val request = getHttpRequestForEndpoint("unknown/issuer");
            request.setRequestURI("unknown/issuer");
            val response = new MockHttpServletResponse();
            request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("casuser:Mellon"));
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.BAD_REQUEST, entity.getStatus());
        }

        @Test
        void verifyPasses() {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("casuser:Mellon"));
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.OK, entity.getStatus());
            assertTrue(entity.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        }

        @Test
        void verifyAuthFails() {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("casuser:unknown"));
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatus());
        }

        @Test
        void verifyAuthMissing() {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatus());
        }
    }
}
