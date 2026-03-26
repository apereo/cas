package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcCredentialEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDCWeb")
class OidcCredentialEndpointControllerTests {

    @ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.stub.attributes.given_name=CAS",
        "cas.authn.attribute-repository.stub.attributes.family_name=User",
        "cas.authn.attribute-repository.stub.attributes.email=casuser@example.org",
        "cas.authn.attribute-repository.stub.attributes.student_id=S12345",
        "cas.authn.attribute-repository.stub.attributes.active=true",
        "cas.authn.attribute-repository.stub.attributes.score=95.5",
        "cas.authn.attribute-repository.stub.attributes.roles=admin,user",

        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.format=vc+sd-jwt",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.scope=UniversityIDCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.given_name.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.family_name.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.email.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.student_id.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.active.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.active.value-type=boolean",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.score.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.score.value-type=number",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.roles.mandatory=false",
        "cas.authn.oidc.vc.issuer.credential-configurations.myorg.claims.roles.value-type=array",

        "cas.authn.oidc.vc.issuer.credential-configurations.strict.format=vc+sd-jwt",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.scope=StrictCredential",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.national_id.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.national_id.value-type=string",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.tax_number.mandatory=true",
        "cas.authn.oidc.vc.issuer.credential-configurations.strict.claims.tax_number.value-type=string"
    })
    abstract static class BaseTests extends AbstractOidcTests {
        protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();

        protected static final String CREDENTIAL_ENDPOINT_URL =
            "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL;

        @Autowired
        @Qualifier("oidcCredentialIssuerMetadataService")
        protected OidcCredentialIssuerMetadataService oidcCredentialIssuerMetadataService;
    }

    @Nested
    class CredentialIssuanceTests extends BaseTests {

        @Test
        void verifyCredentialIssuanceWithBearerToken() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            val response = mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
            assertNotNull(response);
        }

        @Test
        void verifyCredentialIssuanceWithAccessTokenParam() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.ACCESS_TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithTokenParam() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param(OAuth20Constants.TOKEN, accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithOptionalClaimMissing() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "student_id", List.of("S12345")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential").exists());
        }

        @Test
        void verifyCredentialIssuanceWithAllClaimTypes() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS"),
                    "family_name", List.of("User"),
                    "email", List.of("casuser@example.org"),
                    "student_id", List.of("S12345"),
                    "active", List.of("true"),
                    "score", List.of("95.5"),
                    "roles", List.of("admin", "user")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("vc+sd-jwt"))
                .andExpect(jsonPath("$.credential").exists());
        }
    }

    @Nested
    class CredentialIssuanceFailureTests extends BaseTests {
        @Test
        void verifyMissingAccessTokenReturnsError() throws Throwable {
            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyInvalidAccessTokenReturnsError() throws Throwable {
            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer AT-invalid-token-id")
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyMissingMandatoryClaimThrowsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("given_name", List.of("CAS")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("strict");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyMissingAllMandatoryClaimsThrowsError() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val registeredService = getOidcRegisteredService(clientId);
            servicesManager.save(registeredService);

            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("email", List.of("casuser@example.org")));
            val accessToken = getAccessToken(principal, clientId);
            ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
            ticketRegistry.addTicket(accessToken);

            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("strict");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getId())
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void verifyMissingContentTypeReturnsError() throws Throwable {
            val request = new VerifiableCredentialRequest();
            request.setCredentialConfigurationId("myorg");

            mockMvc.perform(get(CREDENTIAL_ENDPOINT_URL)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer AT-12345")
                    .content(MAPPER.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class MetadataServiceTests extends BaseTests {

        @Test
        void verifyMetadataBuild() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            assertNotNull(metadata);
            assertEquals(casProperties.getAuthn().getOidc().getCore().getIssuer(), metadata.getCredentialIssuer());
            assertNotNull(metadata.getAuthorizationServers());
            assertFalse(metadata.getAuthorizationServers().isEmpty());
            assertEquals(1, metadata.getAuthorizationServers().size());
            assertEquals(casProperties.getAuthn().getOidc().getCore().getIssuer(), metadata.getAuthorizationServers().getFirst());
        }

        @Test
        void verifyMetadataCredentialEndpoint() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val expectedEndpoint = casProperties.getAuthn().getOidc().getCore().getIssuer()
                + '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL;
            assertEquals(expectedEndpoint, metadata.getCredentialEndpoint());
        }

        @Test
        void verifyMetadataCredentialConfigurationsSupported() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            assertNotNull(metadata.getCredentialConfigurationsSupported());
            assertFalse(metadata.getCredentialConfigurationsSupported().isEmpty());
            assertTrue(metadata.getCredentialConfigurationsSupported().containsKey("myorg"));
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertEquals("vc+sd-jwt", cfg.getFormat());
            assertEquals("UniversityIDCredential", cfg.getScope());
        }

        @Test
        void verifyMetadataClaimsConfiguration() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertNotNull(cfg.getClaims());
            assertEquals(7, cfg.getClaims().size());

            val givenNameClaim = cfg.getClaims().get("given_name");
            assertNotNull(givenNameClaim);
            assertTrue(givenNameClaim.getMandatory());
            assertEquals("string", givenNameClaim.getValueType());

            val familyNameClaim = cfg.getClaims().get("family_name");
            assertNotNull(familyNameClaim);
            assertTrue(familyNameClaim.getMandatory());
            assertEquals("string", familyNameClaim.getValueType());

            val emailClaim = cfg.getClaims().get("email");
            assertNotNull(emailClaim);
            assertFalse(emailClaim.getMandatory());
            assertEquals("string", emailClaim.getValueType());

            val studentIdClaim = cfg.getClaims().get("student_id");
            assertNotNull(studentIdClaim);
            assertTrue(studentIdClaim.getMandatory());
            assertEquals("string", studentIdClaim.getValueType());

            val activeClaim = cfg.getClaims().get("active");
            assertNotNull(activeClaim);
            assertFalse(activeClaim.getMandatory());
            assertEquals("boolean", activeClaim.getValueType());

            val scoreClaim = cfg.getClaims().get("score");
            assertNotNull(scoreClaim);
            assertFalse(scoreClaim.getMandatory());
            assertEquals("number", scoreClaim.getValueType());

            val rolesClaim = cfg.getClaims().get("roles");
            assertNotNull(rolesClaim);
            assertFalse(rolesClaim.getMandatory());
            assertEquals("array", rolesClaim.getValueType());
        }

        @Test
        void verifyMetadataProofTypesSupported() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertNotNull(cfg.getProofTypesSupported());
            assertFalse(cfg.getProofTypesSupported().isEmpty());
            assertTrue(cfg.getProofTypesSupported().containsKey("jwt"));
            val proof = cfg.getProofTypesSupported().get("jwt");
            assertNotNull(proof.getProofSigningAlgValuesSupported());
            assertFalse(proof.getProofSigningAlgValuesSupported().isEmpty());
        }

        @Test
        void verifyMetadataCryptoBindingAndSigningAlgs() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertNotNull(cfg.getCryptographicBindingMethodsSupported());
            assertFalse(cfg.getCryptographicBindingMethodsSupported().isEmpty());
            assertNotNull(cfg.getCredentialSigningAlgValuesSupported());
            assertFalse(cfg.getCredentialSigningAlgValuesSupported().isEmpty());
        }

        @Test
        void verifyMetadataDefaultValues() {
            val metadata = oidcCredentialIssuerMetadataService.build();
            val cfg = metadata.getCredentialConfigurationsSupported().get("myorg");
            assertTrue(cfg.getCryptographicBindingMethodsSupported().contains("jwk"));
            assertTrue(cfg.getCredentialSigningAlgValuesSupported().contains("ES256"));
            assertTrue(cfg.getCredentialSigningAlgValuesSupported().contains("RS256"));

            val proof = cfg.getProofTypesSupported().get("jwt");
            assertTrue(proof.getProofSigningAlgValuesSupported().contains("ES256"));
            assertTrue(proof.getProofSigningAlgValuesSupported().contains("RS256"));
        }
    }
}
