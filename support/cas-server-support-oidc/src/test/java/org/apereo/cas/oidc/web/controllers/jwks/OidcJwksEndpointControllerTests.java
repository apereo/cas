package org.apereo.cas.oidc.web.controllers.jwks;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Locale;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcJwksEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDCWeb")
class OidcJwksEndpointControllerTests extends AbstractOidcTests {

    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.JWKS_URL)
                .queryParam("state", JsonWebKeyLifecycleStates.CURRENT.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk());
    }

    @Test
    void verifyBadEndpointRequest() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.JWKS_URL)
                .queryParam("state", JsonWebKeyLifecycleStates.CURRENT.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor())
                .with(r -> {
                    r.setServerName("sso2.example.org");
                    return r;
                })
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyUnknownKid() throws Exception {
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.JWKS_URL)
                .queryParam("state", JsonWebKeyLifecycleStates.CURRENT.name().toLowerCase(Locale.ENGLISH))
                .queryParam("kid", "unknown")
                .with(withHttpRequestProcessor())
            )
            .andExpect(status().isOk());
    }
}
