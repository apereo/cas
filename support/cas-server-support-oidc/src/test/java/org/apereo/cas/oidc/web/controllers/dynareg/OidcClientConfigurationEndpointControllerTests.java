package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.OidcRegisteredService;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcClientConfigurationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "cas.authn.oidc.registration.client-secret-expiration=PT1H")
class OidcClientConfigurationEndpointControllerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcClientConfigurationEndpointController")
    protected OidcClientConfigurationEndpointController controller;

    @Test
    void verifyBadEndpointRequest() throws Throwable {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        var mv = controller.handleRequestInternal(StringUtils.EMPTY, request, response);
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, mv.getStatusCode());

        mv = controller.handleUpdates(UUID.randomUUID().toString(), StringUtils.EMPTY, request, response);
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, mv.getStatusCode());
    }

    @Test
    void verifyServiceNotFoundForUpdate() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.CLIENT_CONFIGURATION_URL);
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        assertEquals(HttpStatus.SC_BAD_REQUEST,
            controller.handleUpdates(clientId, null, request, response).getStatusCode().value());
    }

    @Test
    void verifyGetOperation() {
        val request = getHttpRequestForEndpoint(OidcConstants.CLIENT_CONFIGURATION_URL);
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(clientId);
        service.markAsDynamicallyRegistered();
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(
            ZonedDateTime.now(Clock.systemUTC()).toString()));
        servicesManager.save(service);
        assertEquals(HttpStatus.SC_OK,
            controller.handleRequestInternal(clientId, request, response).getStatusCode().value());
    }

    @Test
    void verifyUpdateOperation() throws Throwable {
        val request = getHttpRequestForEndpoint(OidcConstants.CLIENT_CONFIGURATION_URL);
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        var service = getOidcRegisteredService(clientId);
        val clientSecretExpiration = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).toEpochSecond();
        service.setClientSecretExpiration(clientSecretExpiration);
        servicesManager.save(service);

        val jsonBody = """
            {"redirect_uris": ["https://apereo.github.io"],
            "client_name": "Apereo Blog",
            "contacts": ["cas@example.org"],
            "grant_types": ["client_credentials"],
            "introspection_signed_response_alg": "RS256",
            "introspection_encrypted_response_alg": "RSA1_5",
            "introspection_encrypted_response_enc": "A128CBC-HS256"
            }""";
        val responseEntity = controller.handleUpdates(clientId, jsonBody, request, response);
        assertEquals(HttpStatus.SC_OK, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        service = servicesManager.findServiceBy(service.getId(), OidcRegisteredService.class);
        assertNotEquals(service.getClientSecretExpiration(), clientSecretExpiration);
    }

    @Test
    void verifyBadRequest() {
        val request = getHttpRequestForEndpoint(OidcConstants.CLIENT_CONFIGURATION_URL);
        val response = new MockHttpServletResponse();
        val clientId = UUID.randomUUID().toString();
        assertEquals(HttpStatus.SC_BAD_REQUEST,
            controller.handleRequestInternal(clientId, request, response).getStatusCode().value());
    }
}
