package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialNonceEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDCWeb")
class OidcVerifiableCredentialNonceEndpointControllerTests {

    private static final String NONCE_ENDPOINT_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_NONCE_URL;

    @Nested
    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    class NonceEndpointTests extends AbstractOidcTests {
        @Test
        void verifyNonceEndpointIsPubliclyAccessible() throws Throwable {
            mockMvc.perform(post(NONCE_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$." + OidcConstants.C_NONCE).exists())
                .andExpect(jsonPath("$." + OidcConstants.C_NONCE_EXPIRES_IN).doesNotExist());
        }
    }

    @Nested
    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = "cas.authn.oidc.core.issuer=https://wrong.example.org/cas/oidc")
    class NonceEndpointInvalidIssuerTests extends AbstractOidcTests {
        @Test
        void verifyInvalidIssuerReturnsError() throws Throwable {
            mockMvc.perform(post(NONCE_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
        }
    }
}
