package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Tag("OIDC")
public class OidcInitialAccessTokenControllerTests {

    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-mode=OPEN")
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class OpenRegistrationTests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcInitialAccessTokenController")
        protected OidcInitialAccessTokenController controller;

        @Test
        public void verifyNotAllowed() throws Exception {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.NOT_ACCEPTABLE, entity.getStatusCode());
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.oidc.registration.dynamic-client-registration-mode=PROTECTED",
        "cas.authn.oidc.registration.initial-access-token-user=casuser",
        "cas.authn.oidc.registration.initial-access-token-password=Mellon"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class ProtectedRegistrationTests extends AbstractOidcTests {
        @Autowired
        @Qualifier("oidcInitialAccessTokenController")
        protected OidcInitialAccessTokenController controller;

        @Test
        public void verifyMismatchedEndpoint() throws Exception {
            val request = getHttpRequestForEndpoint("unknown/issuer");
            request.setRequestURI("unknown/issuer");
            val response = new MockHttpServletResponse();
            request.addHeader("Authorization", "Basic " + EncodingUtils.encodeBase64("casuser:Mellon"));
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
        }

        @Test
        public void verifyPasses() throws Exception {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            request.addHeader("Authorization", "Basic " + EncodingUtils.encodeBase64("casuser:Mellon"));
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        }

        @Test
        public void verifyAuthFails() throws Exception {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            request.addHeader("Authorization", "Basic " + EncodingUtils.encodeBase64("casuser:unknown"));
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        }

        @Test
        public void verifyAuthMissing() throws Exception {
            val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL);
            val response = new MockHttpServletResponse();
            val entity = controller.handleRequestInternal(request, response);
            assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        }
    }
}
