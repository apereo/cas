package org.apereo.cas.oidc.web.controllers;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyOperationWithValidTicketAsJwtSignedEncrypted() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val credentials = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
        oidcRegisteredService.setIntrospectionEncryptedResponseAlg("RSA-OAEP-256");
        oidcRegisteredService.setIntrospectionEncryptedResponseEncoding("A128CBC-HS256");
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);

        val response = mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE))
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertInstanceOf(EncryptedJWT.class, JWTParser.parse(response));
    }

    @Test
    void verifyOperationWithValidTicketAsJwtSigned() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val credentials = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(AlgorithmIdentifiers.RSA_USING_SHA512);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);

        val response = mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertInstanceOf(SignedJWT.class, JWTParser.parse(response));
    }

    @Test
    void verifyOperationWithValidTicketAsJwtSignedWithNone() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val credentials = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void verifyOperationWithValidTicketAsJwtSignedWithNoneEncryption() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val credentials = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionEncryptedResponseAlg(AlgorithmIdentifiers.NONE);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void verifyOperationWithValidTicketAsJwtPlain() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        val credentials = EncodingUtils.encodeBase64((accessToken.getClientId() + ":secret").getBytes(StandardCharsets.UTF_8));

        val oidcRegisteredService = getOidcRegisteredService(accessToken.getClientId());
        oidcRegisteredService.setIntrospectionSignedResponseAlg(null);
        servicesManager.save(oidcRegisteredService);
        ticketRegistry.addTicket(accessToken);

        val response = mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.ACCEPT, OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(OAuth20Constants.INTROSPECTION_JWT_HEADER_CONTENT_TYPE))
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertInstanceOf(PlainJWT.class, JWTParser.parse(response));
    }

    @Test
    void verifyOperationWithValidTicket() throws Throwable {
        val credentials = EncodingUtils.encodeBase64("clientid:secret".getBytes(StandardCharsets.UTF_8));

        val accessToken = getAccessToken();
        servicesManager.save(getOidcRegisteredService());
        ticketRegistry.addTicket(accessToken);

        val response = mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        val body = MAPPER.readValue(response, Map.class);
        val exp = ((Number) body.get("exp")).longValue();
        val iat = ((Number) body.get("iat")).longValue();
        assertTrue(Instant.ofEpochSecond(exp).isAfter(Instant.ofEpochSecond(iat)));
        assertEquals(accessToken.getScopes(), Set.of(body.get("scope").toString().split(" ")));
    }

    @Test
    void verifyBadEndpointRequest() throws Throwable {
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(request -> {
                    request.setServerName("unknown.issuer.org");
                    return request;
                }))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyOperationWithInvalidTicket() throws Throwable {
        val credentials = EncodingUtils.encodeBase64("clientid:secret".getBytes(StandardCharsets.UTF_8));

        val accessToken = getAccessToken();
        servicesManager.save(getOidcRegisteredService());

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }
}
