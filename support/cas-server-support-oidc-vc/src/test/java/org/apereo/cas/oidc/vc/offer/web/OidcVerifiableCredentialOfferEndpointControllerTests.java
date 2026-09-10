package org.apereo.cas.oidc.vc.offer.web;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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
class OidcVerifiableCredentialOfferEndpointControllerTests {

    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.format=DC_SD_JWT",
        "cas.authn.oidc.vc.issuer.credential-configurations.UniversityDegreeCredential.scope=UniversityDegree"
    })
    abstract static class BaseTests extends AbstractOidcTests {
        protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();

        protected static final String OFFER_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_URL;

        protected static final String TRANSACTIONS_URL = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_OFFER_TRANSACTIONS_URL;
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifyFetchCredentialOffer() throws Exception {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);

            val transaction = createOfferTransaction(registeredService);
            val transactionId = JsonPath.read(transaction, "$.transactionId").toString();
            val txCode = JsonPath.read(transaction, "$.txCode").toString();
            assertNotEquals(transactionId, txCode);

            val responseBody = mockMvc.perform(get(OFFER_URL + '/' + transactionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential_issuer").value(casProperties.getAuthn().getOidc().getCore().getIssuer()))
                .andExpect(jsonPath("$.credential_configuration_ids[0]").value("UniversityDegreeCredential"))
                .andExpect(jsonPath("$.grants").exists())
                .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].tx_code.description").exists())
                .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].tx_code.input_mode").exists())
                .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].tx_code.length").exists())
                .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").exists())
                .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].issuer_state").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

            assertFalse(responseBody.contains(txCode), "The credential offer must never disclose the transaction code");

            val preAuthorizedCode = JsonPath.read(responseBody,
                "$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").toString();
            assertNotNull(preAuthorizedCode);

            mockMvc.perform(tokenExchange(registeredService, preAuthorizedCode, transactionId))
                .andExpect(status().is4xxClientError());

            mockMvc.perform(tokenExchange(registeredService, preAuthorizedCode, null))
                .andExpect(status().is4xxClientError());

            mockMvc.perform(tokenExchange(registeredService, preAuthorizedCode, txCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + OAuth20Constants.ACCESS_TOKEN).exists())
                .andExpect(jsonPath("$." + OAuth20Constants.TOKEN_TYPE).exists())
                .andExpect(jsonPath("$." + OAuth20Constants.EXPIRES_IN).exists())
                .andExpect(jsonPath("$." + OidcConstants.C_NONCE).exists())
                .andExpect(jsonPath("$." + OidcConstants.C_NONCE_EXPIRES_IN).exists());
        }

        private String createOfferTransaction(final OidcRegisteredService registeredService) throws Exception {
            val requestBody = MAPPER.writeValueAsString(
                Map.of("principal", "casuser",
                    "credentialConfigurationIds", List.of("UniversityDegreeCredential")));
            return mockMvc.perform(post(TRANSACTIONS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.txCode").exists())
                .andReturn().getResponse().getContentAsString();
        }

        private static MockHttpServletRequestBuilder tokenExchange(final OidcRegisteredService registeredService,
                                                                   final String preAuthorizedCode,
                                                                   @Nullable final String txCode) {
            val builder = post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType())
                .queryParam(OidcConstants.PRE_AUTHORIZED_CODE, preAuthorizedCode);
            return txCode == null ? builder : builder.queryParam(OidcConstants.TX_CODE, txCode);
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
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.txCode").exists())
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
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue())
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
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue())
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.vc.offer.transaction-code-enabled=false")
    class TransactionCodeDisabledTests extends BaseTests {
        @Test
        void verifyOfferWithoutTransactionCodeCanBeRedeemed() throws Exception {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);

            val requestBody = MAPPER.writeValueAsString(
                Map.of("principal", "casuser",
                    "credentialConfigurationIds", List.of("UniversityDegreeCredential")));
            val transaction = mockMvc.perform(post(TRANSACTIONS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.txCode").doesNotExist())
                .andReturn().getResponse().getContentAsString();
            val transactionId = JsonPath.read(transaction, "$.transactionId").toString();

            val responseBody = mockMvc.perform(get(OFFER_URL + '/' + transactionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].tx_code").doesNotExist())
                .andReturn().getResponse().getContentAsString();
            val preAuthorizedCode = JsonPath.read(responseBody,
                "$.grants.['urn:ietf:params:oauth:grant-type:pre-authorized_code'].pre-authorized_code").toString();

            mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                    .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType())
                    .queryParam(OidcConstants.PRE_AUTHORIZED_CODE, preAuthorizedCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + OAuth20Constants.ACCESS_TOKEN).exists());
        }
    }

    @Nested
    @Execution(ExecutionMode.SAME_THREAD)
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.mapped.people.casuser=eduPersonAffiliation->faculty,name->casuser",

        "cas.authn.oidc.vc.offer.required-principal-attribute=eduPersonAffiliation",
        "cas.authn.oidc.vc.offer.required-principal-attribute-value=faculty|staff"
    })
    class PrincipalAttributeTests extends BaseTests {
        @Test
        void verifyOfferTransactionForRequiredAttribute() throws Exception {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(registeredService);

            performOfferTransaction("casuser", registeredService)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.credentialOfferUri").exists());

            performOfferTransaction("unknown", registeredService)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
        }

        private ResultActions performOfferTransaction(
            final String username,
            final OidcRegisteredService registeredService) throws Exception {
            val requestBody = MAPPER.writeValueAsString(
                Map.of("principal", username,
                    "credentialConfigurationIds", List.of("UniversityDegreeCredential")));
            return mockMvc.perform(post(TRANSACTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue()));
        }
    }
}
