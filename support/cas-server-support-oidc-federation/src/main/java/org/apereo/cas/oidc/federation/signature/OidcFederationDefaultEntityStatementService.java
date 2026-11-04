package org.apereo.cas.oidc.federation.signature;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustMarkIssuerProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationTrustMarkOwnerProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.federation.entities.CommonFederationClaimsSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import static org.apereo.cas.oidc.OidcConstants.KEYS;

/**
 * This is {@link OidcFederationDefaultEntityStatementService}.
 *
 * @author Misagh Moayyed
 * @author Jerome LELEU
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcFederationDefaultEntityStatementService implements OidcFederationEntityStatementService {
    private static final String TRUST_MARKS_CLAIM_NAME = "trust_marks";

    private static final String TRUST_MARK_TYPE_CLAIM_NAME = "trust_mark_type";

    private static final String TRUST_MARK_CLAIM_NAME = "trust_mark";

    private static final String TRUST_MARK_ISSUERS_CLAIM_NAME = "trust_mark_issuers";

    private static final String TRUST_MARK_OWNERS_CLAIM_NAME = "trust_mark_owners";

    private static final int TRUST_MARK_REFERENCE_LENGTH = 64;

    private final OidcFederationJsonWebKeystoreService jsonWebKeystoreService;
    private final OidcProperties oidcProperties;

    /**
     * Create and sign an entity statement. Without federation keys, the statement is the entity configuration of this
     * entity and carries its own keys; with them, it is a subordinate statement about the subject and carries only the
     * subject's keys (OpenID Federation 1.0, section 3), which is what the next statement of a trust chain is verified with.
     * Only subordinate statements carry {@code constraints}, which an entity configuration must not (section 3.1.3).
     * Only the entity configuration carries the {@code trust_marks} issued to this entity (section 3.1.1), and
     * only the entity configuration of a trust anchor carries the {@code trust_mark_issuers} it accepts (section 3.1).
     * The statement is valid from now, to the second, for the configured entity statement expiration. A subordinate
     * statement may leave out {@code metadata} and carry a {@code metadata_policy} among the additional claims instead
     * (section 6.1).
     *
     * @param issuer           the issuer
     * @param subject          the subject
     * @param metadata         the metadata
     * @param federationKeys   the federation keys of the subject of a subordinate statement
     * @param authorityHints   the authority hints
     * @param additionalClaims the additional claims
     * @return the entity statement
     * @throws Exception the exception
     */
    @Override
    public EntityStatement createAndSign(final String issuer,
                                         final String subject,
                                         @Nullable final JSONObject metadata,
                                         @Nullable final JsonNode federationKeys,
                                         @Nullable final List<EntityID> authorityHints,
                                         final Map<String, Object> additionalClaims)
            throws Exception {
        val iss = new EntityID(issuer);
        val sub = new EntityID(subject);

        val now = Instant.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS);
        val entityStatementExpiration = Beans.newDuration(oidcProperties.getFederation().getEntityStatementExpiration());
        val jwks = federationKeys != null
            ? JWKSet.parse(Map.of(KEYS, JSONValue.parse(federationKeys.toString())))
            : jsonWebKeystoreService.toJWKSet();
        val claims = new EntityStatementClaimsSet(
            iss,
            sub,
            Date.from(now),
            Date.from(now.plus(entityStatementExpiration)),
            jwks
        );

        if (authorityHints != null && authorityHints.size() > 0) {
            claims.setAuthorityHints(authorityHints);
        }

        if (metadata != null) {
            claims.setClaim(CommonFederationClaimsSet.METADATA_CLAIM_NAME, metadata);
        }
        additionalClaims.forEach(claims::setClaim);

        if (federationKeys == null && issuer.equals(subject)) {
            val trustMarks = resolveTrustMarks(subject);
            if (!trustMarks.isEmpty()) {
                claims.setClaim(TRUST_MARKS_CLAIM_NAME, trustMarks);
            }
            val trustMarkIssuers = resolveTrustMarkIssuers();
            if (!trustMarkIssuers.isEmpty()) {
                claims.setClaim(TRUST_MARK_ISSUERS_CLAIM_NAME, trustMarkIssuers);
            }
            val trustMarkOwners = resolveTrustMarkOwners();
            if (!trustMarkOwners.isEmpty()) {
                claims.setClaim(TRUST_MARK_OWNERS_CLAIM_NAME, trustMarkOwners);
            }
        }

        return jsonWebKeystoreService.signEntityStatement(claims);
    }

    /**
     * Trust marks issued to this entity, each as an object with {@code trust_mark_type} and {@code trust_mark}
     * (OpenID Federation 1.0, section 3.1.1). The type is taken from the trust mark itself, which must name it.
     * A trust mark whose {@code sub} is not this entity, or that has expired, is left out: a party resolving this
     * entity would reject it, and it would only hide that the mark needs renewing.
     *
     * @param entityId the entity identifier of this entity
     * @return the trust marks to publish
     */
    protected List<Map<String, String>> resolveTrustMarks(final String entityId) {
        return oidcProperties.getFederation().getTrustMarks()
            .stream()
            .map(configured -> readTrustMark(configured, entityId))
            .flatMap(Optional::stream)
            .toList();
    }

    /**
     * The trust mark types a trust anchor accepts and their issuers, published as {@code trust_mark_issuers}
     * (OpenID Federation 1.0, section 3.1). Only a trust anchor publishes them; others would be ignored.
     *
     * @return the trust mark types and their accepted issuers
     */
    protected Map<String, List<String>> resolveTrustMarkIssuers() {
        val federation = oidcProperties.getFederation();
        if (federation.getRole() != OidcFederationRole.TRUST_ANCHOR) {
            return Map.of();
        }
        return federation.getTrustMarkIssuers()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getTrustMarkType()))
            .collect(Collectors.toMap(OidcFederationTrustMarkIssuerProperties::getTrustMarkType,
                entry -> List.copyOf(entry.getIssuers()), (first, second) -> first, LinkedHashMap::new));
    }

    /**
     * The owners of trust mark types whose issuance is delegated, with the keys their delegations are verified with,
     * published as {@code trust_mark_owners} (OpenID Federation 1.0, section 7.2). Only a trust anchor publishes them,
     * and only their public keys; an owner whose keys cannot be read is left out and reported in the logs.
     *
     * @return the trust mark types and their owners
     */
    protected Map<String, Map<String, Object>> resolveTrustMarkOwners() {
        val federation = oidcProperties.getFederation();
        if (federation.getRole() != OidcFederationRole.TRUST_ANCHOR) {
            return Map.of();
        }
        val owners = new LinkedHashMap<String, Map<String, Object>>();
        federation.getTrustMarkOwners()
            .stream()
            .filter(entry -> StringUtils.isNoneBlank(entry.getTrustMarkType(), entry.getOwner(), entry.getJwks()))
            .forEach(entry -> readOwnerKeys(entry).ifPresent(keys ->
                owners.putIfAbsent(entry.getTrustMarkType(), Map.of("sub", entry.getOwner(), "jwks", keys))));
        return owners;
    }

    private static Optional<Map<String, Object>> readOwnerKeys(final OidcFederationTrustMarkOwnerProperties owner) {
        try {
            val value = StringUtils.trim(SpringExpressionLanguageValueResolver.getInstance().resolve(owner.getJwks()));
            val json = Strings.CS.startsWith(value, "{")
                ? value
                : IOUtils.toString(ResourceUtils.getRawResourceFrom(value).getInputStream(), StandardCharsets.UTF_8);
            return Optional.of(JWKSet.parse(json).toPublicJWKSet().toJSONObject());
        } catch (final Exception e) {
            LOGGER.warn("Unable to read the keys of [{}], owner of trust mark type [{}]: [{}]",
                owner.getOwner(), owner.getTrustMarkType(), e.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<Map<String, String>> readTrustMark(final String configured, final String entityId) {
        val reference = StringUtils.abbreviate(configured, TRUST_MARK_REFERENCE_LENGTH);
        try {
            val value = StringUtils.trim(SpringExpressionLanguageValueResolver.getInstance().resolve(configured));
            val serialized = Strings.CS.contains(value, ":")
                ? StringUtils.trim(IOUtils.toString(ResourceUtils.getRawResourceFrom(value).getInputStream(), StandardCharsets.UTF_8))
                : value;
            val claims = SignedJWT.parse(serialized).getJWTClaimsSet();
            val type = claims.getStringClaim(TRUST_MARK_TYPE_CLAIM_NAME);
            if (StringUtils.isBlank(type)) {
                LOGGER.warn("Trust mark [{}] names no trust mark type and is not published", reference);
                return Optional.empty();
            }
            if (!entityId.equals(claims.getSubject())) {
                LOGGER.warn("Trust mark [{}] of type [{}] is about [{}], not [{}], and is not published",
                    reference, type, claims.getSubject(), entityId);
                return Optional.empty();
            }
            val expiration = claims.getExpirationTime();
            if (expiration != null && expiration.toInstant().isBefore(Instant.now(Clock.systemUTC()))) {
                LOGGER.warn("Trust mark [{}] of type [{}] expired at [{}] and is not published", reference, type, expiration);
                return Optional.empty();
            }
            return Optional.of(Map.of(TRUST_MARK_TYPE_CLAIM_NAME, type, TRUST_MARK_CLAIM_NAME, serialized));
        } catch (final Exception e) {
            LOGGER.warn("Unable to read trust mark [{}]: [{}]", reference, e.getMessage());
            return Optional.empty();
        }
    }
}
