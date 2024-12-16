package org.apereo.cas.oidc.authn;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20AuthenticationClientProvider;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.keys.AesKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJwtAuthenticatorHMacTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAuthentication")
@TestPropertySource(properties = "cas.authn.oauth.code.time-to-kill-in-seconds=60")
class OidcJwtAuthenticatorHMacTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcJwtClientProvider")
    private OAuth20AuthenticationClientProvider oidcJwtClientProvider;

    @Test
    void verifyBadAlgAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val registeredService = getOidcRegisteredService();
        servicesManager.save(registeredService);

        val claims = getClaims(registeredService.getClientId(),
            oidcIssuerService.determineIssuer(Optional.of(registeredService)),
            registeredService.getClientId(), registeredService.getClientId());
        val keyGen = KeyPairGenerator.getInstance("RSA");
        val pair = keyGen.generateKeyPair();
        val privateKey = pair.getPrivate();

        val jwt = EncodingUtils.signJwsRSASha512(privateKey, claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());
        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        auth.validate(new CallContext(context, new JEESessionStore()), credentials);
        assertNull(credentials.getUserProfile());
    }

    private Authenticator getAuthenticator() {
        val client = (BaseClient) oidcJwtClientProvider.createClient();
        return client.getAuthenticator();
    }

    @Test
    void verifyAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val claims = getClaims(registeredService.getClientId(),
            oidcIssuerService.determineIssuer(Optional.of(registeredService)),
            registeredService.getClientId(), registeredService.getClientId());

        val key = EncodingUtils.generateJsonWebKey(512);
        registeredService.setJwks(key);
        servicesManager.save(registeredService);

        val jwt = EncodingUtils.signJwsHMACSha512(new AesKey(key.getBytes(StandardCharsets.UTF_8)),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());

        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        auth.validate(new CallContext(context, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
    }

    @Test
    void verifyDisabledServiceAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val audience = casProperties.getServer().getPrefix().concat('/'
            + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
        servicesManager.save(registeredService);

        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
            registeredService.getClientId(), audience);

        val key = EncodingUtils.generateJsonWebKey(512);
        val jwt = EncodingUtils.signJwsHMACSha512(new AesKey(key.getBytes(StandardCharsets.UTF_8)),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());

        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        auth.validate(new CallContext(context, new JEESessionStore()), credentials);
        assertNull(credentials.getUserProfile());
    }

    @Test
    void verifyNoUserAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val registeredService = getOidcRegisteredService();
        val credentials = getCredentials(request, "unknown", "----", registeredService.getClientId());
        auth.validate(new CallContext(context, new JEESessionStore()), credentials);
        assertNull(credentials.getUserProfile());
    }

    @Test
    void verifyBadJwt() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val registeredService = getOidcRegisteredService();
        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            "----", registeredService.getClientId());
        auth.validate(new CallContext(context, new JEESessionStore()), credentials);
        assertNull(credentials.getUserProfile());
    }

    private UsernamePasswordCredentials getCredentials(final MockHttpServletRequest request,
                                                       final String uid, final String password,
                                                       final String clientId) throws Throwable {
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
