package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenJwtBearerGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDC")
class OidcAccessTokenJwtBearerGrantRequestExtractorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("accessTokenJwtBearerGrantRequestExtractor")
    private AccessTokenGrantRequestExtractor extractor;
    
    private WebContext webContext;
    private MockHttpServletRequest request;
    private OidcRegisteredService registeredService;

    @BeforeEach
    void setup() {
        registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.JWT_BEARER.getType()));
        servicesManager.save(registeredService);
        request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        webContext = new JEEContext(request, response);
    }

    @Test
    void verifyUnknownClientId() throws Throwable {
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        request.setParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());
        val assertion = JwtBuilder.buildPlain(JWTClaimsSet.parse(Map.of()), Optional.of(registeredService));
        request.setParameter(OAuth20Constants.ASSERTION, assertion);

        assertTrue(extractor.supports(webContext));
        assertEquals(OAuth20ResponseTypes.NONE, extractor.getResponseType());
        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(webContext));
    }

    @Test
    void verifyPlainAssertion() throws Throwable {
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        request.setParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());
        val claims = JWTClaimsSet.parse(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()));
        val assertion = JwtBuilder.buildPlain(claims, Optional.of(registeredService));
        request.setParameter(OAuth20Constants.ASSERTION, assertion);
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(webContext));
    }

    @Test
    void verifyAssertion() throws Throwable {
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        request.setParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());

        val claims = JWTClaimsSet.parse(Map.of(
            OAuth20Constants.CLAIM_EXP, LocalDateTime.now(Clock.systemUTC()).plusDays(1).toEpochSecond(ZoneOffset.UTC),
            OAuth20Constants.CLAIM_SUB, "casuser",
            OidcConstants.ISS, registeredService.getClientId(),
            OAuth20Constants.CLIENT_ID, registeredService.getClientId(),
            OidcConstants.AUD, casProperties.getServer().getPrefix() + '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL
        ));
        val jsonWebKey = (PublicJsonWebKey) oidcServiceJsonWebKeystoreCache
            .get(new OidcJsonWebKeyCacheKey(registeredService, OidcJsonWebKeyUsage.SIGNING))
            .orElseThrow()
            .getJsonWebKeys()
            .getFirst();
        val signAssertion = EncodingUtils.signJwsRSASha512(jsonWebKey.getPrivateKey(),
            claims.toString().getBytes(StandardCharsets.UTF_8), Map.of());
        request.setParameter(OAuth20Constants.ASSERTION, new String(signAssertion, StandardCharsets.UTF_8));
        val tokenRequestContext = extractor.extract(webContext);
        assertNotNull(tokenRequestContext);
        assertNotNull(tokenRequestContext.getAuthentication());
    }
}
