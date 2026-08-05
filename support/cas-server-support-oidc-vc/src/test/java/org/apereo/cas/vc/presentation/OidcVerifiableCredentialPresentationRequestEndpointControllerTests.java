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
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
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
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree"
})
class OidcVerifiableCredentialPresentationRequestEndpointControllerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String PRESENTATION_REQUEST_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL;

    @Test
    void verifyCreatePresentationRequest() throws Throwable {
        val request = buildPresentationRequest();
        val responseBody = mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.request_id").isNotEmpty())
            .andExpect(jsonPath("$.request_uri").isNotEmpty())
            .andExpect(jsonPath("$.authorization_request").isNotEmpty())
            .andExpect(jsonPath("$.expires_in").isNumber())
            .andReturn()
            .getResponse()
            .getContentAsString();

        val response = MAPPER.readValue(responseBody, OidcVerifiableCredentialPresentationResponse.class);
        assertTrue(response.getRequestId().startsWith(TransientSessionTicket.PREFIX));
        val issuer = casProperties.getAuthn().getOidc().getCore().getIssuer();
        val expectedRequestUri = issuer + '/' + OidcConstants.VC_PRESENTATION_REQUEST_URL + '/' + response.getRequestId();
        assertEquals(expectedRequestUri, response.getRequestUri());

        val clientId = "redirect_uri:" + issuer + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
        val expectedAuthorizationRequest = UriComponentsBuilder
            .fromUriString("openid4vp://authorize")
            .queryParam(OAuth20Constants.CLIENT_ID, clientId)
            .queryParam(OidcConstants.REQUEST_URI, expectedRequestUri)
            .build()
            .encode()
            .toUriString();
        assertEquals(expectedAuthorizationRequest, response.getAuthorizationRequest());

        val ticket = ticketRegistry.getTicket(response.getRequestId(), TransientSessionTicket.class);
        assertNotNull(ticket);
        assertNotNull(ticket.getPropertyAsString("nonce"));
        assertEquals(ticket.getId(), ticket.getPropertyAsString("state"));
        assertEquals(ticket.getExpirationPolicy().getTimeToLive(), response.getExpiresIn());
        val credentials = ticket.getProperty("credentials", List.class);
        assertNotNull(credentials);
        assertEquals(1, credentials.size());
        val credential = assertInstanceOf(CredentialRequest.class, credentials.getFirst());
        assertEquals("university-degree", credential.getId());
        assertEquals("dc+sd-jwt", credential.getFormat());
        assertEquals(List.of("UniversityDegreeCredential"), credential.getVctValues());
        assertEquals(List.of("given_name"), credential.getClaims().getFirst().getPath());
    }

    @Test
    void verifyFetchPresentationRequest() throws Throwable {
        val nonce = UUID.randomUUID().toString();
        val state = UUID.randomUUID().toString();
        val factory = (TransientSessionTicketFactory) defaultTicketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(CollectionUtils.wrap(
            "nonce", nonce,
            "state", state,
            "credentials", buildPresentationRequest().getCredentials()));
        ticketRegistry.addTicket(ticket);

        val issuer = casProperties.getAuthn().getOidc().getCore().getIssuer();
        val expectedClientId = "redirect_uri:" + issuer + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
        val expectedResponseUri = issuer + '/' + OidcConstants.VC_PRESENTATION_RESPONSE_URL;
        mockMvc.perform(get(PRESENTATION_REQUEST_ENDPOINT_URL + '/' + ticket.getId())
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.client_id").value(expectedClientId))
            .andExpect(jsonPath("$.response_uri").value(expectedResponseUri))
            .andExpect(jsonPath("$.response_type").value("vp_token"))
            .andExpect(jsonPath("$.response_mode").value("direct_post"))
            .andExpect(jsonPath("$.nonce").value(nonce))
            .andExpect(jsonPath("$.state").value(state))
            .andExpect(jsonPath("$.dcql_query.credentials[0].id").value("university-degree"))
            .andExpect(jsonPath("$.dcql_query.credentials[0].format").value("dc+sd-jwt"))
            .andExpect(jsonPath("$.dcql_query.credentials[0].meta.vct_values[0]").value("UniversityDegreeCredential"))
            .andExpect(jsonPath("$.dcql_query.credentials[0].claims[0].path[0]").value("given_name"))
            .andExpect(jsonPath("$.client_metadata.client_name").value("Apereo CAS"))
            .andExpect(jsonPath("$.client_metadata.vp_formats_supported['dc+sd-jwt'].alg_values[0]").value("ES256"))
            .andExpect(jsonPath("$.client_metadata.vp_formats_supported['dc+sd-jwt']['sd-jwt_alg_values'][1]").value("ES384"))
            .andExpect(jsonPath("$.client_metadata.vp_formats_supported['dc+sd-jwt']['kb-jwt_alg_values'][2]").value("ES512"));
    }

    @Test
    void verifyEmptyPresentationRequestIsRejected() throws Exception {
        mockMvc.perform(post(PRESENTATION_REQUEST_ENDPOINT_URL)
                .with(withHttpRequestProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"credentials\":[]}"))
            .andExpect(status().isBadRequest());
    }

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
}
