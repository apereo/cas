package org.apereo.cas.oidc.federation.signature;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import net.minidev.json.JSONObject;
import org.jspecify.annotations.Nullable;
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
    default EntityStatement createAndSign(final String issuer,
                                          final String subject,
                                          final JSONObject metadata,
                                          @Nullable final JsonNode federationKeys,
                                          @Nullable final List<EntityID> authorityHints)
            throws Exception {
        return createAndSign(issuer, subject, metadata, federationKeys, authorityHints, Map.of());
    }

    /**
     * Create entity statement with additional claims, such as the {@code metadata_policy} of a subordinate statement.
     *
     * @param issuer           the issuer
     * @param subject          the subject
     * @param metadata         the metadata, which a subordinate statement may leave out
     * @param federationKeys   the federation keys
     * @param authorityHints   the authority hints
     * @param additionalClaims the additional claims
     * @return the entity statement
     * @throws Exception the exception
     */
    EntityStatement createAndSign(String issuer,
                                  String subject,
                                  @Nullable JSONObject metadata,
                                  @Nullable JsonNode federationKeys,
                                  @Nullable List<EntityID> authorityHints,
                                  Map<String, Object> additionalClaims)
            throws Exception;
}
