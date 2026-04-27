package org.apereo.cas.oidc.federation.signature;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import net.minidev.json.JSONObject;
import tools.jackson.databind.JsonNode;

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
     * @param issuer the issuer
     * @param subject the subject
     * @param metadata the metadata
     * @param federationKeys the federation keys
     * @param authorityHints the authority hints
     * @return the entity statement
     * @throws Exception the exception
     */
    EntityStatement createAndSign(String issuer, String subject, JSONObject metadata, JsonNode federationKeys,
                                  List<EntityID> authorityHints) throws Exception;
}
