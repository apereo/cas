package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.AbstractOidcIntermediateFederationTests;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcListFederationEndpointControllerTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("OIDCWeb")
class OidcListFederationEndpointControllerTests {

    private static final String LIST_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + OidcConstants.LIST_FEDERATION_URL;

    private static final String LIST = "[\"http://intermediate\",\"http://rpnometadata\",\"http://rpnokeys\",\"http://op\",\"http://rp\"]";

    @Nested
    class TrustAnchorListEndpointTests extends AbstractOidcTrustAnchorFederationTests {

        @Test
        void verifyInvalidIssuer() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL)
                            .with(request -> {
                                request.setScheme("https");
                                request.setServerName("unknown.example.org");
                                request.setContextPath("/cas");
                                request.setServletPath("/cas");
                                request.setServerPort(443);
                                return request;
                            }))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                    .andExpect(jsonPath("$.error_description").value("Invalid issuer"));
        }

        @Test
        void verifyUnsupportedEntityType() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?entity_type=x")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The entity_type parameter is not supported"));
        }

        @Test
        void verifyUnsupportedTrustMarked() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?trust_marked=x")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The trust_marked parameter is not supported"));
        }

        @Test
        void verifyUnsupportedTrustMarkType() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?trust_mark_type=x")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The trust_mark_type parameter is not supported"));
        }

        @Test
        void verifyUnsupportedIntermediate() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?intermediate=x")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The intermediate parameter is not supported"));
        }

        @Test
        void verifyOperation() throws Exception {
            val result = mockMvc.perform(get(LIST_ENDPOINT_URL)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            assertEquals(LIST, result.getResponse().getContentAsString());
        }
    }

    @Nested
    class IntermediateListEndpointTests extends AbstractOidcIntermediateFederationTests {

        @Test
        void verifyInvalidIssuer() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL)
                            .with(request -> {
                                request.setScheme("https");
                                request.setServerName("unknown.example.org");
                                request.setContextPath("/cas");
                                request.setServletPath("/cas");
                                request.setServerPort(443);
                                return request;
                            }))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
                    .andExpect(jsonPath("$.error_description").value("Invalid issuer"));
        }

        @Test
        void verifyUnsupportedEntityType() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?entity_type=y")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The entity_type parameter is not supported"));
        }

        @Test
        void verifyUnsupportedTrustMarked() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?trust_marked=y")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The trust_marked parameter is not supported"));
        }

        @Test
        void verifyUnsupportedTrustMarkType() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?trust_mark_type=y")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The trust_mark_type parameter is not supported"));
        }

        @Test
        void verifyUnsupportedIntermediate() throws Exception {
            mockMvc.perform(get(LIST_ENDPOINT_URL + "?intermediate=y")
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(OidcConstants.UNSUPPORTED_PARAMETER))
                    .andExpect(jsonPath("$.error_description").value("The intermediate parameter is not supported"));
        }

        @Test
        void verifyOperation() throws Exception {
            val result = mockMvc.perform(get(LIST_ENDPOINT_URL)
                            .with(withHttpRequestProcessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            assertEquals(LIST, result.getResponse().getContentAsString());
        }
    }
}
