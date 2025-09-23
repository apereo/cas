package org.apereo.cas.oidc.federation;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;

/**
 * This is {@link OidcFederationJsonWebKeystoreService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public interface OidcFederationJsonWebKeystoreService {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcFederationWebKeystoreService";

    /**
     * Convert to JWK set.
     */
    JWKSet toJWKSet() throws Exception;

    /**
     * Sign entity statement.
     *
     * @param claims the claims
     * @return the entity statement
     * @throws Exception the exception
     */
    EntityStatement signEntityStatement(EntityStatementClaimsSet claims) throws Exception;
}
