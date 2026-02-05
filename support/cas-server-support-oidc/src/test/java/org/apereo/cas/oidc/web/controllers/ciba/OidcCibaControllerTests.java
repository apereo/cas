package org.apereo.cas.oidc.web.controllers.ciba;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcCibaControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",

    "cas.http-client.host-name-verifier=none",

    "cas.authn.attribute-repository.stub.attributes.email=casuser@apereo.org",
    "cas.authn.attribute-repository.stub.attributes.name=CAS",

    "cas.authn.oidc.ciba.verification.delay=PT1S",
    "cas.authn.oidc.ciba.verification.mail.html=false",
    "cas.authn.oidc.ciba.verification.mail.from=cas@apereo.org",
    "cas.authn.oidc.ciba.verification.mail.subject=CIBA Token",
    "cas.authn.oidc.ciba.verification.mail.text=URL is ${verificationUrl}"
})
@EnabledIfListeningOnPort(port = 25000)
class OidcCibaControllerTests extends AbstractOidcTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyRequestWithoutScopes() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithMultipleHints() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.LOGIN_HINT_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithInvalidUserCode() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.USER_CODE, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());

        registeredService.setBackchannelUserCodeParameterSupported(true);
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithMissingClientToken() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithBadNotificationEndpoint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        registeredService.setBackchannelClientNotificationEndpoint("http://ciba.example.org");

        servicesManager.save(registeredService);
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithIdTokenHint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        registeredService.setBackchannelClientNotificationEndpoint("https://ciba.example.org");
        registeredService.setGenerateRefreshToken(true);
        servicesManager.save(registeredService);

        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());

        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(getAccessToken(registeredService.getClientId()))
            .userProfile(profile)
            .responseType(OAuth20ResponseTypes.CODE)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .registeredService(registeredService)
            .build();
        val idTokenHint = oidcIdTokenGenerator.generate(idTokenContext).token();

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.ID_TOKEN_HINT, idTokenHint)
            )
            .andExpect(status().isOk());
    }

    @Test
    void verifyRequestWithPushDelivery() throws Throwable {
        try (val webServer = new MockWebServer(true)) {
            val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
            registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
            registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
            registeredService.setBackchannelClientNotificationEndpoint("https://localhost:%s".formatted(webServer.getPort()));
            registeredService.setBackchannelUserCodeParameterSupported(true);
            registeredService.setGenerateRefreshToken(true);
            servicesManager.save(registeredService);
            val userCode = UUID.randomUUID().toString();
            var response = mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                        registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                    .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                    .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                    .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                    .queryParam(OidcConstants.BINDING_MESSAGE, UUID.randomUUID().toString())
                    .queryParam(OidcConstants.USER_CODE, userCode)
                    .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            val authRequestId = MAPPER.readValue(response, Map.class).get(OidcConstants.AUTH_REQ_ID).toString();
            assertNotNull(authRequestId);
            Thread.sleep(3000);

            val verifyUrl = "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL + '/' + registeredService.getClientId() + '/' + authRequestId;
            val result = mockMvc.perform(get(verifyUrl)
                    .secure(true)
                    .with(withHttpRequestProcessor()))
                .andExpect(status().isOk())
                .andReturn();
            assertNotNull(result.getModelAndView());
            assertEquals(OidcConstants.CIBA_VERIFICATION_VIEW, result.getModelAndView().getViewName());
            assertTrue(result.getModelAndView().getModel().containsKey("registeredService"));
            assertTrue(result.getModelAndView().getModel().containsKey("cibaRequest"));
            assertTrue(result.getModelAndView().getModel().containsKey("bindingMessage"));
            assertTrue(result.getModelAndView().getModel().containsKey("userCodeRequired"));

            webServer.start();

            val csrfToken = (CsrfToken) result.getRequest().getAttribute("_csrf");
            mockMvc.perform(post(verifyUrl)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .header(csrfToken.getHeaderName(), csrfToken.getToken())
                    .queryParam(csrfToken.getParameterName(), csrfToken.getToken())
                    .cookie(result.getResponse().getCookies())
                )
                .andExpect(status().isBadRequest());


            mockMvc.perform(post(verifyUrl)
                    .secure(true)
                    .with(withHttpRequestProcessor())
                    .header(csrfToken.getHeaderName(), csrfToken.getToken())
                    .queryParam(csrfToken.getParameterName(), csrfToken.getToken())
                    .queryParam("userCode", userCode)
                    .cookie(result.getResponse().getCookies())
                )
                .andExpect(status().isOk());
        }
    }

    @Test
    void verifyUnauthorizedRequest() throws Throwable {
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .with(withHttpRequestProcessor())
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isUnauthorized());
    }
}
