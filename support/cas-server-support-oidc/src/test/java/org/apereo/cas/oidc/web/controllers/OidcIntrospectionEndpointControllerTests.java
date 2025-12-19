package org.apereo.cas.oidc.web.controllers;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.introspection.OidcIntrospectionEndpointController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.HttpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIntrospectionEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = {
    "cas.authn.oidc.discovery.introspection-signed-response-alg-values-supported=RS256,RS384,RS512",
    "cas.authn.oidc.discovery.introspection-encrypted-response-alg-values-supported=RSA-OAEP-256"
})
class OidcIntrospectionEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcIntrospectionEndpointController")
    protected OidcIntrospectionEndpointController oidcIntrospectionEndpointController;

    @Test
    void verifyOperationWithValidTicketAsJwtSignedEncrypted() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.INTROSPECTION_URL);
        val response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE);

        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val value = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
        oidcRegisteredService.setIntrospectionEncryptedResponseAlg("RSA-OAEP-256");
        oidcRegisteredService.setIntrospectionEncryptedResponseEncoding("A128CBC-HS256");
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertInstanceOf(EncryptedJWT.class, JWTParser.parse(result.getBody().toString()));
        assertEquals(OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE, result.getHeaders().getContentType().toString());
    }

    @Test
    void verifyOperationWithValidTicketAsJwtSigned() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.INTROSPECTION_URL);
        val response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE);

        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val value = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(AlgorithmIdentifiers.RSA_USING_SHA512);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response).getBody();
        assertInstanceOf(SignedJWT.class, JWTParser.parse(result.toString()));
    }

    @Test
    void verifyOperationWithValidTicketAsJwtSignedWithNone() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.INTROSPECTION_URL);
        val response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE);

        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val value = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void verifyOperationWithValidTicketAsJwtSignedWithNoneEncryption() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.INTROSPECTION_URL);
        val response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE);

        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val value = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void verifyOperationWithValidTicketAsJwtPlain() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.INTROSPECTION_URL);
        val response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE);

        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val value = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(null);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertInstanceOf(PlainJWT.class, JWTParser.parse(result.getBody().toString()));
        assertEquals(OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE, result.getHeaders().getContentType().toString());
    }

    @Test
    void verifyOperationWithValidTicket() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.INTROSPECTION_URL);
        val response = new MockHttpServletResponse();

        val value = EncodingUtils.encodeBase64("clientid:secret".getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val accessToken = getAccessToken();
        servicesManager.save(getOidcRegisteredService());
        ticketRegistry.addTicket(accessToken);
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        val body = (OAuth20IntrospectionAccessTokenResponse) result.getBody();
        assertNotNull(body);
        assertTrue(Instant.ofEpochSecond(body.getExp()).isAfter(Instant.ofEpochSecond(body.getIat())));
        assertTrue(body.isActive());
        assertEquals(accessToken.getScopes(), Set.of(body.getScope().split(" ")));
    }

    @Test
    void verifyBadEndpointRequest() throws Throwable {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.BAD_REQUEST, mv.getStatusCode());
    }

    @Test
    void verifyOperationWithInvalidTicket() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.INTROSPECTION_URL);
        val response = new MockHttpServletResponse();

        val auth = "clientid:secret";
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        val accessToken = getAccessToken();
        servicesManager.save(getOidcRegisteredService());
        request.addParameter(OAuth20Constants.TOKEN, accessToken.getId());
        val result = oidcIntrospectionEndpointController.handleRequest(request, response);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        val body = (OAuth20IntrospectionAccessTokenResponse) result.getBody();
        assertNotNull(body);
        assertFalse(body.isActive());
    }
}
