package org.apereo.cas.oidc.authn;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPrivateKeyJwtAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties =
    "cas.authn.oauth.code.time-to-kill-in-seconds=60"
)
public class OidcPrivateKeyJwtAuthenticatorTests extends AbstractOidcTests {

    @Test
    public void verifyAction() throws Exception {
        val auth = new OidcPrivateKeyJwtAuthenticator(servicesManager,
            registeredServiceAccessStrategyEnforcer, ticketRegistry,
            webApplicationServiceFactory, casProperties, applicationContext);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val audience = casProperties.getServer().getPrefix().concat('/'
            + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);

        val registeredService = getOidcRegisteredService();
        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
            registeredService.getClientId(), audience);
        val webKey = oidcServiceJsonWebKeystoreCache.get(registeredService).get();
        val jwt = EncodingUtils.signJwsRSASha512(webKey.getPrivateKey(), claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());

        val credentials = getCredential(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());

        auth.validate(credentials, context, JEESessionStore.INSTANCE);
        assertNotNull(credentials.getUserProfile());
    }

    @Test
    public void verifyBadUser() {
        val auth = new OidcPrivateKeyJwtAuthenticator(servicesManager,
            registeredServiceAccessStrategyEnforcer, ticketRegistry,
            webApplicationServiceFactory, casProperties, applicationContext);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val registeredService = getOidcRegisteredService();
        val credentials = getCredential(request, "unknown", "unknown", registeredService.getClientId());

        auth.validate(credentials, context, JEESessionStore.INSTANCE);
        assertNull(credentials.getUserProfile());
    }

    @Test
    public void verifyBadCred() {
        val auth = new OidcPrivateKeyJwtAuthenticator(servicesManager,
            registeredServiceAccessStrategyEnforcer, ticketRegistry,
            webApplicationServiceFactory, casProperties, applicationContext);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val credentials = new UsernamePasswordCredentials(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER, null);
        auth.validate(credentials, context, JEESessionStore.INSTANCE);
        assertNull(credentials.getUserProfile());
    }

    private UsernamePasswordCredentials getCredential(final MockHttpServletRequest request,
                                                      final String uid, final String password, final String clientId) {
        val credentials = new UsernamePasswordCredentials(uid, password);

        val code = defaultOAuthCodeFactory.create(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(),
            StringUtils.EMPTY, StringUtils.EMPTY,
            clientId, new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        request.addParameter(OAuth20Constants.CODE, code.getId());
        return credentials;
    }
}
