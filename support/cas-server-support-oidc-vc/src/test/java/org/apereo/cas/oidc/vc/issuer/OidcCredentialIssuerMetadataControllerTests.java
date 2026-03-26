package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcCredentialIssuerMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDCWeb")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.format=vc+sd-jwt",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.scope=UniversityIDCredential",

    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.value-type=string",

    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.value-type=string",

    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.mandatory=false",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.value-type=string",

    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.value-type=string"
})
class OidcCredentialIssuerMetadataControllerTests extends AbstractOidcTests {

    @Test
    void verifyOperationWithValidTicketAsJwtSignedEncrypted() throws Throwable {
        mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk());
    }

}
