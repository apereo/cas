package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcClientConfigurationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDCWeb")
class OidcClientConfigurationEndpointControllerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=false")
    class DisabledTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                .perform(get("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                )
                .andExpect(status().isNotImplemented());

            mockMvc
                .perform(patch("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                )
                .andExpect(status().isNotImplemented());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.registration.dynamic-client-registration-enabled=true",
        "cas.authn.oidc.registration.client-secret-expiration=PT1H"
    })
    class DefaultTests extends AbstractOidcTests {
        @Test
        void verifyBadEndpointRequest() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            mockMvc
                .perform(get("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(UUID.randomUUID().toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                )
                .andExpect(status().isUnauthorized());

            ticketRegistry.addTicket(accessToken);
            mockMvc
                .perform(get("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .with(r -> {
                        r.setServerName("sso2.example.org");
                        return r;
                    })
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(patch("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                    .with(r -> {
                        r.setServerName("sso2.example.org");
                        return r;
                    })
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(get("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, UUID.randomUUID().toString())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        void verifyGetOperation() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            val service = getOidcRegisteredService(clientId);
            service.markAsDynamicallyRegistered();
            service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(
                ZonedDateTime.now(Clock.systemUTC()).toString()));
            servicesManager.save(service);
            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                .perform(get("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .with(withHttpRequestProcessor())
                )
                .andExpect(status().isOk());
        }

        @Test
        void verifyUpdateOperation() throws Throwable {
            val clientId = UUID.randomUUID().toString();
            var service = getOidcRegisteredService(clientId);
            val clientSecretExpiration = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).toEpochSecond();
            service.setClientSecretExpiration(clientSecretExpiration);
            servicesManager.save(service);

            val jsonBody = """
                {"redirect_uris": ["https://apereo.github.io"],
                "client_name": "Apereo Blog",
                "contacts": ["cas@example.org"],
                "grant_types": ["client_credentials"],
                "introspection_signed_response_alg": "RS256",
                "introspection_encrypted_response_alg": "RSA1_5",
                "introspection_encrypted_response_enc": "A128CBC-HS256"
                }""";

            val accessToken = getAccessToken(clientId, Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
            ticketRegistry.addTicket(accessToken);
            mockMvc
                .perform(patch("/cas/oidc/" + OidcConstants.CLIENT_CONFIGURATION_URL)
                    .param(OAuth20Constants.CLIENT_ID, clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(accessToken.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
                    .with(withHttpRequestProcessor())
                )
                .andExpect(status().isOk());

            service = servicesManager.findServiceBy(service.getId(), OidcRegisteredService.class);
            assertNotEquals(service.getClientSecretExpiration(), clientSecretExpiration);
        }

    }

}
