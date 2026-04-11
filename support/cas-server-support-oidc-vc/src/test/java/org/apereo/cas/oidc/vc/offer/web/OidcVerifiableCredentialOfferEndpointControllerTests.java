package org.apereo.cas.oidc.vc.offer.web;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialOfferEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDCWeb")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=vc+sd-jwt",
    "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree"
})
class OidcVerifiableCredentialOfferEndpointControllerTests extends AbstractOidcTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final String OFFER_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_URL;

    private static final String TRANSACTIONS_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_TRANSACTIONS_URL;

    @Autowired
    @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
    private OidcVerifiableCredentialTransactionService oidcVerifiableCredentialTransactionService;

    @Test
    void verifyFetchCredentialOffer() throws Exception {
        val ticket = oidcVerifiableCredentialTransactionService.issue("casuser", List.of("UniversityDegreeCredential"));
        mockMvc.perform(get(OFFER_URL + '/' + ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.credential_issuer").value(casProperties.getAuthn().getOidc().getCore().getIssuer()))
            .andExpect(jsonPath("$.credential_configuration_ids[0]").value("UniversityDegreeCredential"))
            .andExpect(jsonPath("$.grants").exists())
            .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].tx_code").value(ticket.getId()))
            .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").exists())
            .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].issuer_state").exists());
    }

    @Test
    void verifyFetchUnknownCredentialOffer() throws Exception {
        mockMvc.perform(get(OFFER_URL + "/TST-unknown-id")
                .contentType(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyOfferTransactionIssuance() throws Exception {
        val requestBody = MAPPER.writeValueAsString(
            Map.of("principal", "casuser",
                "credentialConfigurationIds", List.of("UniversityDegreeCredential")));
        mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(withHttpRequestProcessor())
                .with(request -> {
                    val response = new MockHttpServletResponse();
                    val context = new JEEContext(request, response);
                    val profileManager = new ProfileManager(context, oauthDistributedSessionStore);
                    val userProfile = new CommonProfile();
                    userProfile.setId("casuser");
                    userProfile.addAttribute(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId());
                    profileManager.save(true, userProfile, false);
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").exists())
            .andExpect(jsonPath("$.credentialOfferUri").exists());
    }

    @Test
    void verifyOfferTransactionWithUnauthorizedCredentialConfig() throws Exception {
        val requestBody = MAPPER.writeValueAsString(
            Map.of("principal", "casuser",
                "credentialConfigurationIds", List.of("NonExistentCredential")));
        mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(withHttpRequestProcessor())
                .with(request -> {
                    val response = new MockHttpServletResponse();
                    val context = new JEEContext(request, response);
                    val profileManager = new ProfileManager(context, oauthDistributedSessionStore);
                    val userProfile = new CommonProfile();
                    userProfile.setId("casuser");
                    userProfile.addAttribute(OAuth20Constants.CLIENT_ID, getOidcRegisteredService().getClientId());
                    profileManager.save(true, userProfile, false);
                    return request;
                }))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void verifyOfferTransactionWithNoProfile() throws Exception {
        val requestBody = MAPPER.writeValueAsString(
            Map.of("principal", "casuser",
                "credentialConfigurationIds", List.of("UniversityDegreeCredential")));
        mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyOfferTransactionWithNoRequestBody() throws Exception {
        mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isBadRequest());
    }
}
