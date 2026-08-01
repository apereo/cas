package org.apereo.cas.oidc.vc.offer.web;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialTypeMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=vc+sd-jwt",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.display[0].name=University Degree Credential",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.display[0].locale=en-US",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.display[0].description=Digital university degree",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.display[0].logo=https://example.org/logo.png",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.display[0].background-color=#112233",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.display[0].text-color=#ffffff",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.claims.given_name.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.claims.given_name.disclosable=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.claims.given_name.display[0].name=Given Name",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.claims.given_name.display[0].locale=en-US"
})
class OidcVerifiableCredentialTypeMetadataControllerTests extends AbstractOidcTests {
    private static final String CONFIGURATION_ID = "UniversityDegreeCredential";

    private static final String METADATA_ENDPOINT_URL =
        "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL;

    @Test
    void verifyCredentialTypeMetadata() throws Exception {
        mockMvc.perform(get(METADATA_ENDPOINT_URL + '/' + CONFIGURATION_ID)
                .accept(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.vct").value(
                casProperties.getAuthn().getOidc().getCore().getIssuer()
                    + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + '/' + CONFIGURATION_ID))
            .andExpect(jsonPath("$.name").value("University Degree Credential"))
            .andExpect(jsonPath("$.description").value("Digital university degree"))
            .andExpect(jsonPath("$.display[0].name").value("University Degree Credential"))
            .andExpect(jsonPath("$.display[0].locale").value("en-US"))
            .andExpect(jsonPath("$.display[0].description").value("Digital university degree"))
            .andExpect(jsonPath("$.display[0].logo.uri").value("https://example.org/logo.png"))
            .andExpect(jsonPath("$.display[0].logo.alt_text").value("University Degree Credential"))
            .andExpect(jsonPath("$.display[0].backgroundColor").value("#112233"))
            .andExpect(jsonPath("$.display[0].textColor").value("#ffffff"))
            .andExpect(jsonPath("$.claims[0].path[0]").value("given_name"))
            .andExpect(jsonPath("$.claims[0].sd").value("allowed"))
            .andExpect(jsonPath("$.claims[0].display[0].label").value("Given Name"))
            .andExpect(jsonPath("$.claims[0].display[0].lang").value("en-US"));
    }

    @Test
    void verifyUnknownCredentialConfiguration() throws Exception {
        mockMvc.perform(get(METADATA_ENDPOINT_URL + "/UnknownCredential")
                .accept(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    void verifyInvalidIssuer() throws Exception {
        mockMvc.perform(get(METADATA_ENDPOINT_URL + '/' + CONFIGURATION_ID)
                .accept(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor())
                .with(request -> {
                    request.setServerName("unknown.example.org");
                    return request;
                }))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST))
            .andExpect(jsonPath("$.error_description").value("Invalid issuer"));
    }
}
