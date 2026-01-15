package org.apereo.cas.web.saml2;

import module java.base;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DelegatedClientsSamlEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@TestPropertySource(properties = {
    "management.endpoint.delegatedClients.access=UNRESTRICTED",

    "cas.authn.pac4j.saml[0].keystore-path=file:/tmp/keystore-${#randomNumber6}.jks",
    "cas.authn.pac4j.saml[0].keystore-password=1234567890",
    "cas.authn.pac4j.saml[0].private-key-password=1234567890",
    "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=classpath:idp-metadata.xml",
    "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=file:/tmp/sp.xml",
    "cas.authn.pac4j.saml[0].service-provider-entity-id=test-entityid"
})
@Tag("ActuatorEndpoint")
@Import(BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedClientsSamlEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/actuator/delegatedClients")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(delete("/actuator/delegatedClients")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
