package org.jasig.cas.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.OidcConstants;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.OidcRegisteredService;
import org.jasig.cas.support.oauth.OAuthAccessTokenResponseGenerator;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshToken;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * This is {@link OidcAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("oidcAccessTokenResponseGenerator")
public class OidcAccessTokenResponseGenerator extends OAuthAccessTokenResponseGenerator {

    @Value("${cas.oidc.issuer:http://localhost:8080/cas/oidc}")
    private String issuer;

    @Value("${cas.oidc.skew:5}")
    private int skew;

    @Value("${cas.oidc.jwks:}")
    private Resource jwksFile;

    @Override
    protected void generateJsonInternal(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final JsonGenerator jsonGenerator,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId,
                                        final long timeout,
                                        final Service service,
                                        final OAuthRegisteredService registeredService) throws Exception {

        super.generateJsonInternal(request, response, jsonGenerator, accessTokenId, 
                refreshTokenId, timeout, service, registeredService);
        final OidcRegisteredService oidcRegisteredService = (OidcRegisteredService) registeredService;

        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final UserProfile profile = manager.get(true);
        
        final JwtClaims claims = produceIdTokenClaims(request, accessTokenId, timeout, 
                oidcRegisteredService, profile, context);
        final JsonWebKeySet jwks = buildJsonWebKeySet(oidcRegisteredService);
        final String idToken = signIdTokenClaim(oidcRegisteredService, jwks, claims);
        jsonGenerator.writeStringField(OidcConstants.ID_TOKEN, idToken);
    }

    /**
     * Produce id token claims jwt claims.
     *
     * @param request       the request
     * @param accessTokenId the access token id
     * @param timeout       the timeout
     * @param service       the service
     * @param profile       the user profile
     * @param context       the context
     * @return the jwt claims
     */
    protected JwtClaims produceIdTokenClaims(final HttpServletRequest request, 
                                             final AccessToken accessTokenId, final long timeout, 
                                             final OidcRegisteredService service,
                                             final UserProfile profile,
                                             final J2EContext context) {
        final Authentication authentication = accessTokenId.getAuthentication();
        final Principal principal = authentication.getPrincipal();
        
        final JwtClaims claims = new JwtClaims();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(this.issuer);
        claims.setAudience(service.getClientId());
        claims.setExpirationTimeMinutesInTheFuture(timeout);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(this.skew);
        claims.setSubject(principal.getId());
        
        claims.setClaim(OAuthConstants.STATE, authentication.getAttributes().get(OAuthConstants.STATE));
        claims.setClaim(OAuthConstants.NONCE, authentication.getAttributes().get(OAuthConstants.NONCE));
        
        final Sets.SetView<String> setView = Sets.intersection(OidcConstants.CLAIMS, principal.getAttributes().keySet());
        setView.immutableCopy().stream().forEach(k -> claims.setClaim(k, principal.getAttributes().get(k)));
        
        if (!claims.hasClaim(OidcConstants.CLAIM_PREFERRED_USERNAME)) {
            claims.setClaim(OidcConstants.CLAIM_PREFERRED_USERNAME, profile.getId());
        }
        
        return claims;
    }

    /**
     * Sign id token claim string.
     *
     * @param svc    the service
     * @param jwks   the jwks
     * @param claims the claims
     * @return the string
     * @throws JoseException the jose exception
     */
    protected String signIdTokenClaim(final OidcRegisteredService svc,
                                      final JsonWebKeySet jwks,
                                      final JwtClaims claims) throws JoseException {
        final JsonWebSignature jws = new JsonWebSignature();

        final String jsonClaims = claims.toJson();
        jws.setPayload(jsonClaims);
        logger.debug("Generated claims are {}", jsonClaims);

        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
        jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);

        if (svc.isSignIdToken() && !jwks.getJsonWebKeys().isEmpty()) {
            final RsaJsonWebKey jsonWebKey = (RsaJsonWebKey) jwks.getJsonWebKeys().get(0);
            jws.setKey(jsonWebKey.getPrivateKey());
            jws.setAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE);
            if (StringUtils.isBlank(jsonWebKey.getKeyId())) {
                jws.setKeyIdHeaderValue(UUID.randomUUID().toString());
            } else {
                jws.setKeyIdHeaderValue(jsonWebKey.getKeyId());
            }
            logger.debug("Signing id token with key id header value {}", jws.getKeyIdHeaderValue());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        }
        logger.debug("Signing id token with algorithm {}", jws.getAlgorithmHeaderValue());
        return jws.getCompactSerialization();
    }

    /**
     * Build json web key set.
     *
     * @param service the service
     * @return the json web key set
     * @throws Exception the exception
     */
    protected JsonWebKeySet buildJsonWebKeySet(final OidcRegisteredService service) throws Exception {
        JsonWebKeySet jsonWebKeySet = null;
        try {
            if (StringUtils.isNotBlank(service.getJwks())) {
                logger.debug("Loading JWKS from {}", service.getJwks());
                final Resource resource = this.resourceLoader.getResource(service.getJwks());
                jsonWebKeySet = new JsonWebKeySet(IOUtils.toString(resource.getInputStream(), "UTF-8"));
            }
        } catch (final Exception e) {
            logger.debug(e.getMessage(), e);
        } finally {
            if (jsonWebKeySet == null) {
                logger.debug("Loading default JWKS from {}", this.jwksFile);
                final String jsonJwks = IOUtils.toString(this.jwksFile.getInputStream(), "UTF-8");
                jsonWebKeySet = new JsonWebKeySet(jsonJwks);
            }
        }
        return jsonWebKeySet;
    }
}

