package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
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
    void verifyRequestWithPushDelivery() throws Throwable {
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
                .queryParam(OidcConstants.CLIENT_NOTIFICATION_TOKEN, UUID.randomUUID().toString())
                .queryParam(OidcConstants.LOGIN_HINT, UUID.randomUUID().toString())
            )
            .andExpect(status().isOk());
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
