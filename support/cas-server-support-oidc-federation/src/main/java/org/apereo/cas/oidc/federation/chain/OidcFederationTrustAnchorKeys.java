package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Tells whether a trust anchor signed its statements in a trust chain with a key it has since revoked, or with a key it
 * had already retired when it issued them (OpenID Federation 1.0, section 8.7), as published by its federation
 * historical keys endpoint.
 * <p>
 * The keys a trust anchor is trusted with out of band may still list a key it has revoked; its historical keys are
 * verified with those keys, and the entity configuration that advertises them is always fetched from the trust anchor,
 * never taken from the chain: a chain sent by a relying party could carry an entity configuration signed with the
 * revoked key that advertises no historical keys, or other ones. Statements are matched by their {@code kid}, which is
 * required (section 3), so a statement that names no key is treated as signed with a revoked key. When the trust anchor
 * advertises no historical keys endpoint, none of its keys is revoked.
 * <p>
 * The historical keys of a trust anchor are kept for a minute. The request for them does not count against the request
 * budget of the resolution, so that a resolution that used up its budget finding a chain can still check it; the entity
 * configuration of the trust anchor is read through the retriever of the resolution, which counts it unless the
 * resolution has already fetched it, as it has when it resolved a chain to the trust anchor. When they
 * cannot be obtained or verified, whether the key was revoked cannot be told: the chain is not used, and the resolution
 * is unavailable rather than failed, so that the entity is not remembered as unresolved for an outage of the trust
 * anchor.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcFederationTrustAnchorKeys {
    private static final Duration HISTORICAL_KEYS_LIFETIME = Duration.ofMinutes(1);

    private static final int MAX_TRUST_ANCHORS = 1_000;

    private final OidcFederationHttpClient httpClient;

    private final Cache<String, Map<String, JWK>> historicalKeys = Caffeine.newBuilder()
        .expireAfterWrite(HISTORICAL_KEYS_LIFETIME)
        .maximumSize(MAX_TRUST_ANCHORS)
        .build();

    /**
     * Whether the trust anchor signed its statements in the chain with a key it has since revoked, or had retired before.
     *
     * @param trustChainResolver the trust chain resolver of the trust anchor of the chain
     * @param chain              the trust chain
     * @return true or false
     * @throws OidcFederationResolutionUnavailableException when the historical keys of the trust anchor cannot be read
     */
    public boolean isSignedWithRevokedKey(final TrustChainResolver trustChainResolver, final TrustChain chain) {
        val trustAnchorId = chain.getTrustAnchorEntityID();
        val keysByKeyId = readHistoricalKeys(trustChainResolver, trustAnchorId);
        val statements = new ArrayList<EntityStatement>();
        statements.add(chain.getSuperiorStatements().getLast());
        if (chain.getTrustAnchorConfiguration() != null) {
            statements.add(chain.getTrustAnchorConfiguration());
        }
        val revoked = statements.stream().anyMatch(statement -> {
            val keyId = statement.getSignedStatement().getHeader().getKeyID();
            if (StringUtils.isBlank(keyId)) {
                return true;
            }
            val historicalKey = keysByKeyId.get(keyId);
            return historicalKey != null && !OidcFederationHistoricalKeys.mayHaveSigned(historicalKey, statement.getClaimsSet().getIssueTime());
        });
        if (revoked) {
            LOGGER.warn("Trust chain of [{}] is signed with a key that trust anchor [{}] has revoked, or had retired before",
                chain.getLeafConfiguration().getEntityID(), trustAnchorId);
        }
        return revoked;
    }

    private Map<String, JWK> readHistoricalKeys(final TrustChainResolver trustChainResolver, final EntityID trustAnchorId) {
        val trustAnchorKeys = trustChainResolver.getTrustAnchors().get(trustAnchorId);
        val cacheKey = trustAnchorId.getValue() + ' ' + (trustAnchorKeys == null ? StringUtils.EMPTY : trustAnchorKeys.toString(true));
        val cached = historicalKeys.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        try {
            val keysByKeyId = fetchHistoricalKeys(trustChainResolver, trustAnchorId, trustAnchorKeys);
            historicalKeys.put(cacheKey, keysByKeyId);
            return keysByKeyId;
        } catch (final Exception e) {
            LOGGER.warn("Unable to read the historical keys of trust anchor [{}], so its trust chains are not used now: [{}]",
                trustAnchorId, e.getMessage());
            throw new OidcFederationResolutionUnavailableException("Historical keys of trust anchor %s cannot be read now".formatted(trustAnchorId));
        }
    }

    private Map<String, JWK> fetchHistoricalKeys(final TrustChainResolver trustChainResolver, final EntityID trustAnchorId,
                                                 @Nullable final JWKSet trustAnchorKeys)
            throws Exception {
        val configuration = trustChainResolver.getEntityStatementRetriever().fetchEntityConfiguration(trustAnchorId);
        if (trustAnchorKeys == null) {
            configuration.verifySignatureOfSelfStatement();
        } else {
            configuration.verifySignature(trustAnchorKeys);
        }
        val verificationKeys = Objects.requireNonNullElseGet(trustAnchorKeys, () -> configuration.getClaimsSet().getJWKSet());
        return OidcFederationHistoricalKeys.fetch(configuration, verificationKeys, httpClient)
            .stream()
            .filter(key -> key.getKeyID() != null)
            .collect(Collectors.toUnmodifiableMap(JWK::getKeyID, Function.identity(), (first, _) -> first));
    }
}
