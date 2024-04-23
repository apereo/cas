package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcCibaControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
public class OidcCibaControllerTests extends AbstractOidcTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    @Autowired
    @Qualifier("oidcCibaController")
    protected OidcCibaController oidcCibaController;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(applicationContext)
            .apply(springSecurity())
            .defaultRequest(post("/")
                .contextPath("/cas")
                .header("X-Forwarded-Proto", "https")
                .header("Host", "sso.example.org")
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .build();
    }

    @Test
    void verifyRequestWithoutScopes() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .header("Authorization", "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithMultipleHints() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .header("Authorization", "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
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
        mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .header("Authorization", "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.USER_CODE, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());

        registeredService.setBackchannelUserCodeParameterSupported(true);
        servicesManager.save(registeredService);
        mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .header("Authorization", "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
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
        mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .header("Authorization", "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyRequestWithIdTokenHint() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);

        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.addAttributes((Map) RegisteredServiceTestUtils.getTestAttributes());

        val idTokenHint = oidcIdTokenGenerator.generate(getAccessToken(registeredService.getClientId()), profile,
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE, registeredService).token();

        mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .header("Authorization", "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
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
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(OidcBackchannelTokenDeliveryModes.PUSH.getMode());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        servicesManager.save(registeredService);
        val response = mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .header("Authorization", "Basic " + EncodingUtils.encodeBase64("%s:%s".formatted(registeredService.getClientId(),
                    registeredService.getClientSecret()).getBytes(StandardCharsets.UTF_8)))
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        val authRequestId = MAPPER.readValue(response, Map.class).get(OidcConstants.AUTH_REQ_ID).toString();
        val cibaRequest = ticketRegistry.getTicket(authRequestId, OidcCibaRequest.class);
        assertNotNull(cibaRequest);
    }

    @Test
    void verifyUnauthorizedRequest() throws Throwable {
        mvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL)
                .secure(true)
                .queryParam(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope())
                .queryParam(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.name())
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isUnauthorized());
    }
}
