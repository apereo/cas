package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.DefaultOAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationToken;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJwtAuthenticatorRsaTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDCAuthentication")
@TestPropertySource(properties = {
    "cas.authn.oauth.code.time-to-kill-in-seconds=60",
    "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/private-jwks.jwks"
})
class OidcJwtAuthenticatorRsaTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oidcJwtClientProvider")
    private OAuth20ClientAuthenticationProvider oidcJwtClientProvider;

    @Test
    void verifyAction() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService();
        registeredService.setClientId(UUID.randomUUID().toString());

        val file = Files.createTempFile("jwks-service", ".jwks").toFile();
        val core = casProperties.getAuthn().getOidc().getJwks().getCore();
        val jsonWebKey = OidcJsonWebKeyStoreUtils.generateJsonWebKey(
            core.getJwksType(), core.getJwksKeySize(), OidcJsonWebKeyUsage.SIGNING);
        jsonWebKey.setKeyId("cas-kid");

        val jsonWebKeySet = new JsonWebKeySet(jsonWebKey);
        val data = jsonWebKeySet.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
        FileUtils.write(file, data, StandardCharsets.UTF_8);
        registeredService.setJwks("file://" + file.getAbsolutePath());
        servicesManager.save(registeredService);

        val audience = casProperties.getServer().getPrefix().concat('/'
                + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.ACCESS_TOKEN_URL);
        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
                registeredService.getClientId(), audience);
        val webKeys = oidcServiceJsonWebKeystoreCache.get(
            new OidcJsonWebKeyCacheKey(registeredService, OidcJsonWebKeyUsage.SIGNING)).orElseThrow();
        val key = (PublicJsonWebKey) webKeys.getJsonWebKeys().getFirst();
        val jwt = EncodingUtils.signJwsRSASha512(key.getPrivateKey(),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());
        val credentials = getCredential(request, OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8), registeredService.getClientId());

        assertTrue(auth.authenticate(credentials).isPresent());
    }

    @Test
    void verifyBadUser() throws Throwable {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val registeredService = getOidcRegisteredService();
        val credentials = getCredential(request, "unknown", "unknown", registeredService.getClientId());

        assertTrue(auth.authenticate(credentials).isEmpty());
    }

    @Test
    void verifyBadCred() {
        val auth = getAuthenticator();

        val request = new MockHttpServletRequest();
        val credentials = newClientAuthenticationToken(request, OAuth20ClientAuthenticationToken.CredentialSource.FORM,
            OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER, null);
        assertTrue(auth.authenticate(credentials).isEmpty());
    }

    private OidcJwtAuthenticator getAuthenticator() {
        return (OidcJwtAuthenticator) ((DefaultOAuth20ClientAuthenticationProvider) oidcJwtClientProvider).getAuthenticator();
    }

    private OAuth20ClientAuthenticationToken getCredential(final MockHttpServletRequest request,
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
