package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        @Test
        void verifyNotAllowed() throws Throwable {
            mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isNotAcceptable());
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.oidc.registration.dynamic-client-registration-mode=PROTECTED",
        "cas.authn.oidc.registration.initial-access-token-user=casuser",
        "cas.authn.oidc.registration.initial-access-token-password=Mellon"
    })
    @Nested
    class ProtectedRegistrationTests extends AbstractOidcTests {

        @Test
        void verifyMismatchedEndpoint() throws Throwable {
            val credentials = EncodingUtils.encodeBase64("casuser:Mellon");
            mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL)
                    .with(withHttpRequestProcessor())
                    .with(request -> {
                        request.setServerName("unknown.issuer.org");
                        return request;
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials))
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyPasses() throws Throwable {
            val credentials = EncodingUtils.encodeBase64("casuser:Mellon");
            mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists());
        }

        @Test
        void verifyAuthFails() throws Throwable {
            val credentials = EncodingUtils.encodeBase64("casuser:unknown");
            mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void verifyAuthMissing() throws Throwable {
            mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isUnauthorized());
        }
    }
}
