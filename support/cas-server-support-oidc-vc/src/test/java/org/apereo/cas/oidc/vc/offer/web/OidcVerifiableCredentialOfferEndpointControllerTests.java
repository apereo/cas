package org.apereo.cas.oidc.vc.offer.web;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
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
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        
        val ticket = oidcVerifiableCredentialTransactionService.issue(registeredService.getClientId(), "casuser", List.of("UniversityDegreeCredential"));
        val responseBody = mockMvc.perform(get(OFFER_URL + '/' + ticket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.credential_issuer").value(casProperties.getAuthn().getOidc().getCore().getIssuer()))
            .andExpect(jsonPath("$.credential_configuration_ids[0]").value("UniversityDegreeCredential"))
            .andExpect(jsonPath("$.grants").exists())
            .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].tx_code").value(ticket.getId()))
            .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").exists())
            .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].issuer_state").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        val txCode = JsonPath.read(responseBody, "$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].tx_code").toString();
        assertNotNull(txCode);
        val preAuthorizedCode = JsonPath.read(responseBody, "$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").toString();
        assertNotNull(preAuthorizedCode);

        mockMvc
            .perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType())
                .queryParam(OidcConstants.PRE_AUTHORIZED_CODE, preAuthorizedCode)
                .queryParam(OidcConstants.TX_CODE, txCode)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$." + OAuth20Constants.ACCESS_TOKEN).exists())
            .andExpect(jsonPath("$." + OAuth20Constants.TOKEN_TYPE).exists())
            .andExpect(jsonPath("$." + OAuth20Constants.EXPIRES_IN).exists())
            .andExpect(jsonPath("$." + OidcConstants.C_NONCE).exists())
            .andExpect(jsonPath("$." + OidcConstants.C_NONCE_EXPIRES_AT).exists());
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
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        
        val requestBody = MAPPER.writeValueAsString(
            Map.of("principal", "casuser",
                "credentialConfigurationIds", List.of("UniversityDegreeCredential")));
        mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").exists())
            .andExpect(jsonPath("$.credentialOfferUri").exists());
    }

    @Test
    void verifyOfferTransactionWithUnauthorizedCredentialConfig() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        
        val requestBody = MAPPER.writeValueAsString(
            Map.of("principal", "casuser",
                "credentialConfigurationIds", List.of("NonExistentCredential")));
        mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
            )
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
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyOfferTransactionWithNoRequestBody() throws Exception {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
            )
            .andExpect(status().isBadRequest());
    }
}
