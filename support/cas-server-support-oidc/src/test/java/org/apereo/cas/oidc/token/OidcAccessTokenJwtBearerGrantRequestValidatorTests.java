package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.validator.CertificateBoundAccessTokenRequiredException;
import org.apereo.cas.support.oauth.validator.token.OAuth20InvalidTargetException;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.token.JwtBuilder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.dpop.DefaultDPoPProofFactory;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPProofException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenJwtBearerGrantRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDC")
class OidcAccessTokenJwtBearerGrantRequestValidatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAccessTokenJwtBearerGrantTypeTokenRequestValidator")
    private OAuth20TokenRequestValidator validator;

    @Test
    void verifySubjectTokenAsIdToken() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.ASSERTION, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        assertTrue(validator.supports(request, response));
        assertTrue(validator.validate(request, response));
    }

    @Test
    void verifyClientWithDPoPBoundAccessTokens() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setDpopBoundAccessTokens(true);
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest(HttpMethod.POST.name(), "/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.TOKEN_URL);
        request.setScheme("https");
        request.setServerName("sso.example.org");
        request.setServerPort(443);
        val response = new MockHttpServletResponse();
        val claims = JWTClaimsSet.parse(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()));
        request.addParameter(OAuth20Constants.ASSERTION, JwtBuilder.buildPlain(claims, Optional.of(registeredService)));
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        assertThrows(InvalidDPoPProofException.class, () -> validator.validate(request, response));

        val key = new ECKeyGenerator(Curve.P_256).generate();
        val proof = new DefaultDPoPProofFactory(key, JWSAlgorithm.ES256)
            .createDPoPJWT(HttpMethod.POST.name(), new URI(request.getRequestURL().toString()));
        request.addHeader(OAuth20Constants.DPOP, proof.serialize());
        assertTrue(validator.validate(request, response));
        assertEquals(key.computeThumbprint().toString(), request.getAttribute(OAuth20Constants.DPOP_CONFIRMATION));
        assertEquals(proof.serialize(), request.getAttribute(OAuth20Constants.DPOP));
    }

    @Test
    void verifyRequestedResources() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setAllowedResources(Set.of("https://api.example.org/"));
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val claims = JWTClaimsSet.parse(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()));
        request.addParameter(OAuth20Constants.ASSERTION, JwtBuilder.buildPlain(claims, Optional.of(registeredService)));
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        request.addParameter(OAuth20Constants.RESOURCE, "https://api.example.org/");
        assertTrue(validator.validate(request, response));
        request.setParameter(OAuth20Constants.RESOURCE, "https://other.example.org/");
        assertThrows(OAuth20InvalidTargetException.class, () -> validator.validate(request, response));
    }

    @Test
    void verifyClientWithCertificateBoundAccessTokensRequiresTls() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setTlsClientCertificateBoundAccessTokens(true);
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val claims = JWTClaimsSet.parse(Map.of(OAuth20Constants.CLIENT_ID, registeredService.getClientId()));
        request.addParameter(OAuth20Constants.ASSERTION, JwtBuilder.buildPlain(claims, Optional.of(registeredService)));
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        assertThrows(CertificateBoundAccessTokenRequiredException.class, () -> validator.validate(request, response));
    }
}
