package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.DefaultOAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationToken;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.OctetSequenceJsonWebKey;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.HmacKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
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
    private OAuth20ClientAuthenticationProvider oidcJwtClientProvider;

    @Test
    void verifyBadAlgAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService();
        servicesManager.save(registeredService);

        val audience = casProperties.getServer().getPrefix().concat('/'
                + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);
        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
                registeredService.getClientId(), audience);
        val keyGen = KeyPairGenerator.getInstance("RSA");
        val pair = keyGen.generateKeyPair();
        val privateKey = pair.getPrivate();

        val jwt = EncodingUtils.signJwsRSASha512(privateKey, claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());
        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        assertTrue(auth.authenticate(credentials).isEmpty());
    }

    private OidcJwtAuthenticator getAuthenticator() {
        return (OidcJwtAuthenticator) ((DefaultOAuth20ClientAuthenticationProvider) oidcJwtClientProvider).getAuthenticator();
    }

    @Test
    void verifyAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val audience = casProperties.getServer().getPrefix().concat('/'
                + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);
        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
                registeredService.getClientId(), audience);

        val key = EncodingUtils.generateJsonWebKey(512);
        registeredService.setJwks(key);
        servicesManager.save(registeredService);

        val jwt = EncodingUtils.signJwsHMACSha512(new AesKey(key.getBytes(StandardCharsets.UTF_8)),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());

        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        val profile = auth.authenticate(credentials).orElseThrow();
        assertEquals(registeredService.getClientId(), profile.getAttribute(OAuth20Constants.CLIENT_ID));

        val replayRequest = new MockHttpServletRequest();
        val replayCredentials = getCredentials(replayRequest, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        assertTrue(auth.authenticate(replayCredentials).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "client_secret_jwt")
    void verifyClientSecretJwtIsVerifiedWithClientSecret(final String authenticationMethod) throws Throwable {
        val auth = getAuthenticator();
        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setTokenEndpointAuthenticationMethod(authenticationMethod);
        val clientSecret = EncodingUtils.generateJsonWebKey(512);
        registeredService.setClientSecrets(CollectionUtils.wrapList(OAuthRegisteredServiceClientSecret.withoutExpiration(clientSecret)));
        servicesManager.save(registeredService);

        val audience = casProperties.getServer().getPrefix().concat('/'
            + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);
        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
            registeredService.getClientId(), audience);
        val jwt = EncodingUtils.signJwsHMACSha512(new AesKey(clientSecret.getBytes(StandardCharsets.UTF_8)),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());
        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        assertTrue(auth.authenticate(credentials).isPresent());
    }

    @Test
    void verifySharedKeyShorterThanAlgorithmRequiresIsSkipped() throws Throwable {
        val auth = getAuthenticator();
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
            registeredService.getClientId(), casProperties.getServer().getPrefix());
        val sharedKey = EncodingUtils.generateJsonWebKey(512);
        val jwt = JWTParser.parse(new String(EncodingUtils.signJwsHMACSha512(new AesKey(sharedKey.getBytes(StandardCharsets.UTF_8)),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of()), StandardCharsets.UTF_8));
        assertTrue(auth.isVerificationKeyUsable(jwt,
            new OctetSequenceJsonWebKey(new HmacKey(sharedKey.getBytes(StandardCharsets.UTF_8))), registeredService));
        assertFalse(auth.isVerificationKeyUsable(jwt,
            new OctetSequenceJsonWebKey(new HmacKey("secret".repeat(10).getBytes(StandardCharsets.UTF_8))), registeredService));
        assertTrue(auth.isVerificationKeyUsable(jwt,
            new OctetSequenceJsonWebKey(new HmacKey("secret".repeat(11).getBytes(StandardCharsets.UTF_8))), registeredService));
    }

    @Test
    void verifySubjectMustMatchClientId() throws Throwable {
        val auth = getAuthenticator();
        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        val audience = casProperties.getServer().getPrefix().concat('/'
            + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);
        val claims = getClaims("other-client", registeredService.getClientId(),
            registeredService.getClientId(), audience);

        val key = EncodingUtils.generateJsonWebKey(512);
        registeredService.setJwks(key);
        servicesManager.save(registeredService);
        val jwt = EncodingUtils.signJwsHMACSha512(new AesKey(key.getBytes(StandardCharsets.UTF_8)),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());
        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());
        assertTrue(auth.authenticate(credentials).isEmpty());
    }

    @Test
    void verifyDisabledServiceAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();

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
        assertTrue(auth.authenticate(credentials).isEmpty());
    }

    @Test
    void verifyNoUserAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService();
        val credentials = getCredentials(request, "unknown", "----", registeredService.getClientId());
        assertTrue(auth.authenticate(credentials).isEmpty());
    }

    @Test
    void verifyBadJwt() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService();
        val credentials = getCredentials(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            "----", registeredService.getClientId());
        assertTrue(auth.authenticate(credentials).isEmpty());
    }

    private OAuth20ClientAuthenticationToken getCredentials(final MockHttpServletRequest request,
                                                            final String uid, final String password,
                                                            final String clientId)
            throws Throwable {
                val code = defaultOAuthCodeFactory.create(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(),
            StringUtils.EMPTY, StringUtils.EMPTY,
            clientId, new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        request.addParameter(OAuth20Constants.CODE, code.getId());
        return newClientAuthenticationToken(request, OAuth20ClientAuthenticationToken.CredentialSource.FORM, uid, password);
    }
}
