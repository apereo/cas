package org.apereo.cas.oidc.federation.resolve;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationHistoricalKeys;
import org.apereo.cas.oidc.federation.chain.OidcFederationLocalEntityStatementRetriever;
import org.apereo.cas.oidc.federation.chain.OidcFederationMetadataPolicies;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainConstraints;
import org.apereo.cas.oidc.federation.signature.OidcFederationJsonWebKeystoreService;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationTrustMarkValidator;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.EntityStatementRetriever;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/**
 * Resolves entities for a trust anchor or an intermediate (OpenID Federation 1.0, section 8.3). A trust anchor
 * resolves entities to itself; an intermediate resolves them to its trust anchors, or to its authority hints when it
 * defines no trust anchors. Requested trust anchors this entity does not trust are ignored; when none is left, the
 * request fails with {@code invalid_trust_anchor}. The trust chains of the subject are resolved as for automatic
 * registration, with the same bounds on authority hints and path length, while the statements of this entity itself
 * are built locally. The shortest chain to any requested trust anchor that satisfies the constraints of its statements,
 * and is not signed with a key its trust anchor has revoked, is used, ending with the entity configuration of the trust
 * anchor; without one, the request fails with {@code invalid_trust_chain}. The metadata of each entity type of the
 * subject, or of each requested type, is resolved with the metadata policies of the chain; types the constraints of
 * the chain do not allow are left out, and a policy that cannot apply fails the request with {@code invalid_metadata}.
 * Only valid trust marks are included, and the response expires with the chain or the first of those trust marks.
 * <p>
 * Resolved chains, and subjects that cannot be resolved, are kept per subject and trusted trust anchors for at most the
 * retention of unresolved entities, or until the chain or a trust mark expires, so that repeated requests do not cause
 * the chains to be resolved again. The number of kept resolutions is bounded. Responses are signed per request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcFederationDefaultEntityResolver implements OidcFederationEntityResolver {
    /**
     * Type of resolve responses.
     */
    public static final JOSEObjectType RESOLVE_RESPONSE_JWT_TYPE = new JOSEObjectType("resolve-response+jwt");

    private static final String NOT_FOUND = "not_found";

    private static final String INVALID_TRUST_ANCHOR = "invalid_trust_anchor";

    private static final String INVALID_TRUST_CHAIN = "invalid_trust_chain";

    private static final String INVALID_METADATA = "invalid_metadata";

    private static final String METADATA = "metadata";

    private static final String TRUST_CHAIN = "trust_chain";

    private static final String TRUST_MARKS = "trust_marks";

    private static final String TRUST_MARK = "trust_mark";

    private static final int MAX_RESOLUTIONS = 10_000;

    private final OidcFederationProperties properties;

    private final OidcIssuerService issuerService;

    private final OidcFederationJsonWebKeystoreService keystoreService;

    private final OidcFederationTrustMarkValidator trustMarkValidator;

    private final EntityStatementRetriever retriever;

    private final CheckedSupplier<EntityStatement> entityConfiguration;

    private final CheckedFunction<EntityID, Optional<EntityStatement>> subordinateStatements;

    private final Map<EntityID, Map<EntityID, TrustChainResolver>> trustChainResolvers = new ConcurrentHashMap<>();

    private final Cache<String, Resolution> resolutions = Caffeine.newBuilder()
        .maximumSize(MAX_RESOLUTIONS)
        .expireAfter(Expiry.<String, Resolution>creating((key, resolution) -> resolution.lifetime()))
        .build();

    @Override
    public SignedJWT resolve(final String subject, final List<String> trustAnchors, final List<String> entityTypes) throws Exception {
        if (!properties.getRole().isTrustAnchorOrIntermediate()) {
            throw new OidcFederationResolveException(HttpStatus.NOT_FOUND, NOT_FOUND,
                "Federation role [%s] does not resolve entities".formatted(properties.getRole()));
        }
        if (!isEntityIdentifier(subject) || trustAnchors.isEmpty()
            || !trustAnchors.stream().allMatch(OidcFederationDefaultEntityResolver::isEntityIdentifier)) {
            throw new OidcFederationResolveException(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST,
                "Subject and trust anchors must be entity identifiers");
        }
        val entityId = new EntityID(issuerService.determineIssuer(Optional.empty()));
        val resolvers = trustChainResolvers.computeIfAbsent(entityId, this::buildTrustChainResolvers);
        val trusted = trustAnchors
            .stream()
            .map(EntityID::new)
            .filter(resolvers::containsKey)
            .distinct()
            .sorted(Comparator.comparing(EntityID::getValue))
            .toList();
        if (trusted.isEmpty()) {
            throw new OidcFederationResolveException(HttpStatus.NOT_FOUND, INVALID_TRUST_ANCHOR,
                "None of the requested trust anchors is trusted by this entity");
        }
        val key = Stream.concat(Stream.of(subject), trusted.stream().map(EntityID::getValue)).collect(Collectors.joining(" "));
        val resolution = resolutions.get(key, _ -> resolveEntity(entityId, new EntityID(subject), trusted, resolvers));
        if (resolution.failure() != null) {
            throw resolution.failure();
        }
        return sign(entityId, Objects.requireNonNull(resolution.entity()), entityTypes);
    }

    private Map<EntityID, TrustChainResolver> buildTrustChainResolvers(final EntityID entityId) {
        val localRetriever = new OidcFederationLocalEntityStatementRetriever(retriever, entityId, entityConfiguration, subordinateStatements);
        if (properties.getRole() == OidcFederationRole.TRUST_ANCHOR) {
            return Map.of(entityId, OidcFederationDefaultTrustChainResolver.buildTrustChainResolver(entityId, null, properties, localRetriever));
        }
        val resolvers = new LinkedHashMap<EntityID, TrustChainResolver>();
        OidcFederationDefaultTrustChainResolver.buildTrustChainResolvers(properties, localRetriever)
            .forEach(resolver -> resolver.getTrustAnchors().keySet().forEach(trustAnchor -> resolvers.putIfAbsent(trustAnchor, resolver)));
        return resolvers;
    }

    private Resolution resolveEntity(final EntityID entityId, final EntityID subject, final List<EntityID> trustAnchors,
                                     final Map<EntityID, TrustChainResolver> resolvers) {
        val retention = Beans.newDuration(properties.getUnresolvedEntityRetention());
        val candidates = new ArrayList<ResolvedChain>();
        for (val trustAnchor : trustAnchors) {
            val resolver = resolvers.get(trustAnchor);
            try {
                resolveTrustChains(entityId, subject, trustAnchor, resolver)
                    .forEach(chain -> candidates.add(new ResolvedChain(resolver, chain)));
            } catch (final Exception e) {
                LOGGER.debug("Trust chains of [{}] to [{}] cannot be resolved: [{}]", subject, trustAnchor, e.getMessage());
            }
        }
        val selected = candidates.stream().min(Comparator.comparingInt(candidate -> candidate.chain().length()));
        if (selected.isEmpty()) {
            LOGGER.warn("[{}] has no valid trust chain to any of [{}]", subject, trustAnchors);
            return new Resolution(null, new OidcFederationResolveException(HttpStatus.BAD_REQUEST, INVALID_TRUST_CHAIN,
                "No valid trust chain to the requested trust anchors"), retention);
        }
        val chain = selected.get().chain();
        val trustMarks = trustMarkValidator.resolveValidTrustMarks(selected.get().resolver(), chain);
        val expirationTime = Stream.concat(Stream.of(chain.resolveExpirationTime()),
                trustMarks.stream().map(OidcFederationDefaultEntityResolver::readExpirationTime).flatMap(Optional::stream))
            .min(Date::compareTo)
            .orElseThrow();
        val lifetime = Duration.between(Instant.now(Clock.systemUTC()), expirationTime.toInstant());
        val kept = lifetime.compareTo(retention) < 0 ? lifetime : retention;
        return new Resolution(new ResolvedEntity(chain, trustMarks, expirationTime), null, kept.isNegative() ? Duration.ZERO : kept);
    }

    private static List<TrustChain> resolveTrustChains(final EntityID entityId, final EntityID subject, final EntityID trustAnchor,
                                                       final TrustChainResolver resolver)
            throws Exception {
        val chains = resolver.resolveTrustChains(subject)
            .stream()
            .filter(chain -> OidcFederationTrustChainConstraints.isSatisfiedBy(chain, null))
            .toList();
        if (chains.isEmpty()) {
            return List.of();
        }
        val configuration = resolver.getEntityStatementRetriever().fetchEntityConfiguration(trustAnchor);
        val keys = resolver.getTrustAnchors().get(trustAnchor);
        if (keys == null) {
            configuration.verifySignatureOfSelfStatement();
        } else {
            configuration.verifySignature(keys);
        }
        return chains
            .stream()
            .map(chain -> new TrustChain(chain.getLeafConfiguration(), chain.getSuperiorStatements(), configuration))
            .filter(chain -> trustAnchor.equals(entityId) || !OidcFederationHistoricalKeys.isSignedWithRevokedTrustAnchorKey(resolver, chain))
            .toList();
    }

    private SignedJWT sign(final EntityID entityId, final ResolvedEntity entity, final List<String> entityTypes) throws Exception {
        val chain = entity.trustChain();
        val metadata = new JSONObject();
        for (val entityType : readEntityTypes(chain, entityTypes)) {
            if (EntityType.FEDERATION_ENTITY.equals(entityType) || OidcFederationTrustChainConstraints.isSatisfiedBy(chain, entityType)) {
                try {
                    metadata.put(entityType.getValue(), OidcFederationMetadataPolicies.resolveMetadata(chain, entityType));
                } catch (final Exception e) {
                    LOGGER.warn("Metadata of type [{}] of [{}] cannot be resolved: [{}]", entityType, chain.getLeafConfiguration().getEntityID(), e.getMessage());
                    throw new OidcFederationResolveException(HttpStatus.BAD_REQUEST, INVALID_METADATA,
                        "Metadata of type " + entityType + " cannot be resolved with the policies of the trust chain");
                }
            }
        }
        val claims = new JWTClaimsSet.Builder()
            .issuer(entityId.getValue())
            .subject(chain.getLeafConfiguration().getEntityID().getValue())
            .issueTime(new Date())
            .expirationTime(entity.expirationTime())
            .claim(METADATA, metadata)
            .claim(TRUST_CHAIN, chain.toSerializedJWTs());
        if (!entity.trustMarks().isEmpty()) {
            claims.claim(TRUST_MARKS, entity.trustMarks());
        }
        return keystoreService.sign(claims.build(), RESOLVE_RESPONSE_JWT_TYPE);
    }

    private static List<EntityType> readEntityTypes(final TrustChain chain, final List<String> requested) {
        val types = new LinkedHashSet<String>();
        types.addAll(readMetadataTypes(chain.getLeafConfiguration()));
        types.addAll(readMetadataTypes(chain.getSuperiorStatements().getFirst()));
        return types.stream().filter(type -> requested.isEmpty() || requested.contains(type)).map(EntityType::new).toList();
    }

    private static Set<String> readMetadataTypes(final EntityStatement statement) {
        return statement.getClaimsSet().getClaim(METADATA) instanceof final Map<?, ?> metadata
            ? metadata.keySet().stream().map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new))
            : Set.of();
    }

    private static Optional<Date> readExpirationTime(final Map<String, Object> trustMark) {
        return FunctionUtils.doAndHandle(
                () -> Optional.ofNullable(SignedJWT.parse(String.valueOf(trustMark.get(TRUST_MARK))).getJWTClaimsSet().getExpirationTime()),
                e -> Optional.<Date>empty())
            .get();
    }

    private static boolean isEntityIdentifier(@Nullable final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            val uri = new URI(value);
            return Strings.CI.equals(uri.getScheme(), "https") && StringUtils.isNotBlank(uri.getHost())
                && uri.getRawQuery() == null && uri.getRawFragment() == null;
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    private record ResolvedChain(TrustChainResolver resolver, TrustChain chain) {
    }

    private record ResolvedEntity(TrustChain trustChain, List<Map<String, Object>> trustMarks, Date expirationTime) {
    }

    private record Resolution(@Nullable ResolvedEntity entity, @Nullable OidcFederationResolveException failure, Duration lifetime) {
    }
}
