package org.apereo.cas.oidc.federation.resolve;

import module java.base;
import com.nimbusds.jwt.SignedJWT;
import org.jspecify.annotations.Nullable;

/**
 * Resolves an entity of the federation on behalf of others (OpenID Federation 1.0, section 8.3): its trust chain to a
 * trust anchor, its metadata once the policies of that chain apply, and its valid trust marks, in a signed resolve
 * response.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OidcFederationEntityResolver {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcFederationEntityResolver";

    /**
     * Resolve the entity.
     *
     * @param subject      the entity identifier of the entity to resolve
     * @param trustAnchors the trust anchors the entity may be resolved to, any of which may be used
     * @param entityTypes  the entity types whose metadata is returned, or empty for all of them
     * @return the signed resolve response
     * @throws OidcFederationResolveException when the entity cannot be resolved, with the error to answer
     * @throws Exception                      when the resolve response cannot be produced
     */
    default SignedJWT resolve(final String subject, final List<String> trustAnchors, final List<String> entityTypes) throws Exception {
        return resolve(subject, trustAnchors, entityTypes, null);
    }

    /**
     * Resolve the entity for an authenticated requester, to which the resolve response is addressed.
     *
     * @param subject      the entity identifier of the entity to resolve
     * @param trustAnchors the trust anchors the entity may be resolved to, any of which may be used
     * @param entityTypes  the entity types whose metadata is returned, or empty for all of them
     * @param audience     the entity identifier of the authenticated requester, or null when the request is not authenticated
     * @return the signed resolve response
     * @throws OidcFederationResolveException when the entity cannot be resolved, with the error to answer
     * @throws Exception                      when the resolve response cannot be produced
     */
    SignedJWT resolve(String subject, List<String> trustAnchors, List<String> entityTypes, @Nullable String audience) throws Exception;
}
