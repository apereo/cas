package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponse;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "cas.authn.oidc.registration.client-secret-expiration=P14D")
class OidcDynamicClientRegistrationEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcDynamicClientRegistrationEndpointController")
    protected OidcDynamicClientRegistrationEndpointController controller;

    @Test
    void verifyBadEndpointRequest() {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = controller.handleRequestInternal(StringUtils.EMPTY, request, response);
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, mv.getStatusCode());
    }

    @Test
    void verifyBadInput() {
        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();
        assertEquals(HttpStatus.SC_BAD_REQUEST, controller.handleRequestInternal("bad-input", request, response).getStatusCode().value());
    }

    @Test
    void verifyBadRedirect() {
        val registrationReq = """
    {
       "redirect_uris": ["https://client.example.org/callback#something", "https://client.example.org/callback2"],
       "request_uris": ["https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"]
      }
    """;

        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();
        assertEquals(HttpStatus.SC_BAD_REQUEST, controller.handleRequestInternal(registrationReq, request, response).getStatusCode().value());
    }

    @Test
    void verifyOperation() {
        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();

        try (val webServer = new MockWebServer(org.springframework.http.HttpStatus.OK)) {
            webServer.responseBodyJson(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
            webServer.start();

            val registrationReq = """
    {
        "application_type": "web",
        "default_acr_values": ["mfa-duo","mfa-gauth"],
        "redirect_uris": ["https://client.example.org/callback","https://client.example.org/callback2"],
        "client_name": "My Example",
        "client_name#ja-Japan-JP": "Japanese",
        "logo_uri": "https://client.example.org/logo.png",
        "policy_uri": "https://client.example.org/policy",
        "tos_uri": "https://client.example.org/tos",
        "subject_type": "pairwise",
        "sector_identifier_uri": "http://localhost:%s",
        "token_endpoint_auth_method": "client_secret_basic",
        "jwks_uri": "https://client.example.org/my_public_keys.jwks",
        "id_token_signed_response_alg": "RS256",
        "id_token_encrypted_response_alg": "RSA1_5",
        "id_token_encrypted_response_enc": "A128CBC-HS256",
        "userinfo_encrypted_response_alg": "RSA1_5",
        "userinfo_encrypted_response_enc": "A128CBC-HS256",
        "contacts": ["ve7jtb@example.org", "mary@example.org"]
    }
    """.formatted(webServer.getPort());
            val responseEntity = (ResponseEntity<OidcClientRegistrationResponse>) controller.handleRequestInternal(registrationReq, request, response);
            assertEquals(HttpStatus.SC_CREATED, responseEntity.getStatusCode().value());
            assertTrue(responseEntity.getBody().getClientIdIssuedAt() > 0);
        }
    }

    @Test
    void verifyNoClientNameOperation() {
        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();

        try (val webServer = new MockWebServer(org.springframework.http.HttpStatus.OK)) {
            webServer.responseBodyJson(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
            webServer.start();

            val registrationReq = """
    {
        "application_type": "web",
        "default_acr_values": ["mfa-duo", "mfa-gauth"],
        "redirect_uris": ["https://client.example.org/callback", "https://client.example.org/callback2"],
        "client_name#ja-Japan-JP": "Japanese",
        "logo_uri": "https://client.example.org/logo.png",
        "policy_uri": "https://client.example.org/policy",
        "tos_uri": "https://client.example.org/tos",
        "subject_type": "pairwise",
        "sector_identifier_uri": "http://localhost:%s",
        "token_endpoint_auth_method": "client_secret_basic",
        "jwks": {"keys": []},
        "id_token_signed_response_alg": "RS256",
        "id_token_encrypted_response_alg": "RSA1_5",
        "id_token_encrypted_response_enc": "A128CBC-HS256",
        "userinfo_encrypted_response_alg": "RSA1_5",
        "contacts": ["ve7jtb@example.org", "mary@example.org"]
    }
""".formatted(webServer.getPort());

            assertEquals(HttpStatus.SC_CREATED,
                controller.handleRequestInternal(registrationReq, request, response).getStatusCode().value());
        }
    }

    @Test
    void verifyMissingBackchannelEndpoint() {
        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();

        try (val webServer = new MockWebServer(org.springframework.http.HttpStatus.OK)) {
            webServer.responseBodyJson(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
            webServer.start();

            var registrationReq = """
    {
        "application_type": "web",
        "backchannel_token_delivery_mode": "ping",
        "default_acr_values": ["mfa-duo", "mfa-gauth"],
        "redirect_uris": ["https://client.example.org/callback", "https://client.example.org/callback2"],
        "contacts": ["ve7jtb@example.org", "mary@example.org"]
    }
""";
            assertEquals(HttpStatus.SC_BAD_REQUEST,
                controller.handleRequestInternal(registrationReq, request, response).getStatusCode().value());

            registrationReq = """
    {
        "application_type": "web",
        "backchannel_token_delivery_mode": "ping",
        "backchannel_client_notification_endpoint": "http://localhost:9811",
        "default_acr_values": ["mfa-duo", "mfa-gauth"],
        "redirect_uris": ["https://client.example.org/callback", "https://client.example.org/callback2"],
        "contacts": ["ve7jtb@example.org", "mary@example.org"]
    }
""";
            assertEquals(HttpStatus.SC_BAD_REQUEST,
                controller.handleRequestInternal(registrationReq, request, response).getStatusCode().value());
        }
    }
}
