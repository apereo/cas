package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.ClaimRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.CredentialRequest;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationResponse;
import com.google.common.base.Splitter;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialPresentationRequestEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
class OidcVerifiableCredentialPresentationRequestEndpointControllerTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String PRESENTATION_REQUEST_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL;

    private static OidcVerifiableCredentialPresentationRequest buildPresentationRequest() {
        val claim = new ClaimRequest();
        claim.setPath(List.of("given_name"));
        claim.setRequired(true);

        val credential = new CredentialRequest();
        credential.setId("university-degree");
        credential.setFormat("dc+sd-jwt");
        credential.setVctValues(List.of("UniversityDegreeCredential"));
        credential.setClaims(List.of(claim));

        val request = new OidcVerifiableCredentialPresentationRequest();
        request.setCredentials(List.of(credential));
        return request;
    }

    private static Map<String, String> parseQueryParameters(final URI uri) {
        val parameters = new LinkedHashMap<String, String>();
        for (val pair : Splitter.on('&').split(uri.getRawQuery())) {
            val index = pair.indexOf('=');
            parameters.put(URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8),
                URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8));
        }
        return parameters;
    }

    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree"
    })
    abstract static class BaseTests extends AbstractOidcTests {
        protected OidcVerifiableCredentialPresentationResponse createPresentationRequest() throws Exception {
            val responseBody = mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, getOidcRegisteredService().getClientSecrets().getFirst().getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(buildPresentationRequest())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
            return MAPPER.readValue(responseBody, OidcVerifiableCredentialPresentationResponse.class);
        }

        protected String getResponseUri() {
            return casProperties.getAuthn().getOidc().getCore().getIssuer()
                + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
        }

        protected TransientSessionTicket createPresentationTransaction(final String nonce, final String state) throws Throwable {
            val factory = (TransientSessionTicketFactory) defaultTicketFactory.get(TransientSessionTicket.class);
            val ticket = factory.create(CollectionUtils.wrap(
                "nonce", nonce,
                "state", state,
                "credentials", buildPresentationRequest().getCredentials()));
            ticketRegistry.addTicket(ticket);
            return ticket;
        }
    }

    /**
     * OpenID4VP 1.0 forbids signing a request made under the {@code redirect_uri} client
     * identifier prefix, and a request URI response must carry a signed request object.
     * The request is therefore passed to the wallet by value and no request URI is offered.
     */
    @Nested
    class UnsignedRequestTests extends BaseTests {

        @Test
        void verifyPresentationRequestCreationRejectsUnauthenticatedClients() throws Throwable {
            mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(buildPresentationRequest())))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyEmptyPresentationRequestIsRejected() throws Exception {
            mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, getOidcRegisteredService().getClientSecrets().getFirst().getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"credentials\":[]}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyAuthorizationRequestCarriesEveryParameterByValue() throws Throwable {
            val response = createPresentationRequest();
            assertTrue(response.getRequestId().startsWith(TransientSessionTicket.PREFIX));
            assertNull(response.getRequestUri(), "No request URI may be offered for an unsigned request");

            val authorizationRequest = URI.create(response.getAuthorizationRequest());
            assertEquals("openid4vp", authorizationRequest.getScheme());
            val parameters = parseQueryParameters(authorizationRequest);
            assertFalse(parameters.containsKey(OidcConstants.REQUEST_URI));
            assertEquals("redirect_uri:" + getResponseUri(), parameters.get(OAuth20Constants.CLIENT_ID));
            assertEquals("vp_token", parameters.get(OAuth20Constants.RESPONSE_TYPE));
            assertEquals("direct_post", parameters.get(OAuth20Constants.RESPONSE_MODE));
            assertEquals(getResponseUri(), parameters.get("response_uri"));
            assertEquals(response.getRequestId(), parameters.get(OAuth20Constants.STATE));

            val ticket = ticketRegistry.getTicket(response.getRequestId(), TransientSessionTicket.class);
            assertNotNull(ticket);
            assertEquals(ticket.getPropertyAsString("nonce"), parameters.get(OAuth20Constants.NONCE));
            assertEquals(ticket.getExpirationPolicy().getTimeToLive(), response.getExpiresIn());

            val dcqlQuery = MAPPER.readValue(parameters.get("dcql_query"), Map.class);
            val credentials = assertInstanceOf(List.class, dcqlQuery.get("credentials"));
            assertEquals(1, credentials.size());
            val credential = assertInstanceOf(Map.class, credentials.getFirst());
            assertEquals("university-degree", credential.get("id"));
            assertEquals("dc+sd-jwt", credential.get("format"));
            assertEquals(List.of("UniversityDegreeCredential"),
                assertInstanceOf(Map.class, credential.get("meta")).get("vct_values"));
            val claims = assertInstanceOf(List.class, credential.get("claims"));
            assertEquals(List.of("given_name"), assertInstanceOf(Map.class, claims.getFirst()).get("path"));

            val clientMetadata = MAPPER.readValue(parameters.get("client_metadata"), Map.class);
            assertEquals("Apereo CAS", clientMetadata.get("client_name"));
            val vpFormats = assertInstanceOf(Map.class, clientMetadata.get("vp_formats_supported"));
            val sdJwtFormat = assertInstanceOf(Map.class, vpFormats.get("dc+sd-jwt"));
            assertEquals(List.of("ES256", "ES384", "ES512"), sdJwtFormat.get("alg_values"));
            assertEquals(List.of("ES256", "ES384", "ES512"), sdJwtFormat.get("sd-jwt_alg_values"));
            assertEquals(List.of("ES256", "ES384", "ES512"), sdJwtFormat.get("kb-jwt_alg_values"));
        }

        @Test
        void verifyRequestObjectIsNotServedByReference() throws Throwable {
            val ticket = createPresentationTransaction(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isNotFound());
        }
    }

    /**
     * With a client identifier prefix that permits signing, the request object is served by
     * reference as a signed JWT, and the deep link carries only the client identifier and the
     * request URI.
     */
    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.vc.presentation.client-identifier-prefix=X509_SAN_DNS")
    class SignedRequestTests extends BaseTests {

        @Test
        void verifyAuthorizationRequestIsPassedByReference() throws Throwable {
            val response = createPresentationRequest();
            val issuer = casProperties.getAuthn().getOidc().getCore().getIssuer();
            assertEquals(issuer + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL + '/' + response.getRequestId(),
                response.getRequestUri());

            val parameters = parseQueryParameters(URI.create(response.getAuthorizationRequest()));
            assertEquals(Set.of(OAuth20Constants.CLIENT_ID, OidcConstants.REQUEST_URI), parameters.keySet());
            assertEquals("x509_san_dns:" + URI.create(getResponseUri()).getHost(),
                parameters.get(OAuth20Constants.CLIENT_ID));
            assertEquals(response.getRequestUri(), parameters.get(OidcConstants.REQUEST_URI));
        }

        /**
         * The request object media type is set on the response rather than declared as a produces
         * condition, so a wallet with a narrow Accept header reaches the handler instead of being
         * turned away by content negotiation with a 406.
         */
        @Test
        void verifyNarrowAcceptHeaderReachesTheHandler() throws Throwable {
            val ticket = createPresentationTransaction(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                    .with(withHttpRequestProcessor())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyRequestObjectIsRefusedWithoutCertificateChain() throws Throwable {
            val ticket = createPresentationTransaction(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            val response = mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertTrue(response.contains("certificate chain"),
                () -> "Expected a configuration error about the signing certificate chain but got " + response);
        }
    }
}
