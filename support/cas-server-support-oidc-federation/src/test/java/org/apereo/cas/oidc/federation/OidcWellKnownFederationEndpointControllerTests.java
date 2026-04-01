package org.apereo.cas.oidc.federation;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcWellKnownFederationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDCWeb")
class OidcWellKnownFederationEndpointControllerTests extends AbstractOidcFederationTests {

    private static final String FEDERATION_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_FEDERATION_URL;
    
    @Test
    void verifyInvalidIssuer() throws Exception {
        mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                .with(request -> {
                    request.setScheme("https");
                    request.setServerName("unknown.example.org");
                    request.setContextPath("/cas");
                    request.setServletPath("/cas");
                    request.setServerPort(443);
                    return request;
                }))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get(FEDERATION_ENDPOINT_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk());
    }
}
