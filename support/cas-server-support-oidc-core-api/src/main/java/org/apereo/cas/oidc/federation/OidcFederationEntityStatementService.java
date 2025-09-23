package org.apereo.cas.oidc.federation;

import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;

/**
 * This is {@link OidcFederationEntityStatementService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface OidcFederationEntityStatementService {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcFederationEntityStatementService";

    /**
     * Create entity statement.
     *
     * @return the entity statement
     * @throws Exception the exception
     */
    EntityStatement createAndSign() throws Exception;
}
