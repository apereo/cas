package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

/**
 * The {@code constraints} that trust anchors and intermediates set in their subordinate statements, each applied
 * independently to a trust chain (OpenID Federation 1.0, section 6.2). A statement issued by the k-th superior of the
 * subject of the chain constrains the entities below that superior: {@code max_path_length} bounds the k - 1
 * intermediates between them and must not be negative; {@code naming_constraints} restricts the hosts of their entity
 * identifiers with domain names as in RFC 5280, where {@code .example.org} stands for the hosts below it, any other name
 * for that host only, and an excluded name prevails; {@code allowed_entity_types} must allow the entity type the subject
 * is trusted for, since its metadata of that type would otherwise be removed. Members that are not understood are
 * ignored, and a constraint whose value cannot be read invalidates the chain.
 * <p>
 * Every statement of the chain must also name the key it is signed with in its {@code kid} header (section 3). Signature
 * verification otherwise tries every key of the issuer, which would let a statement signed with a key the issuer has
 * revoked, but that is still trusted, escape the checks that match revoked keys by {@code kid}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@UtilityClass
public class OidcFederationTrustChainConstraints {
    private static final String CONSTRAINTS = "constraints";

    private static final String MAX_PATH_LENGTH = "max_path_length";

    private static final String NAMING_CONSTRAINTS = "naming_constraints";

    private static final String ALLOWED_ENTITY_TYPES = "allowed_entity_types";

    /**
     * Whether the trust chain satisfies the constraints of each of its subordinate statements, and each of its
     * statements names the key it is signed with.
     *
     * @param chain          the trust chain
     * @param leafEntityType the entity type the subject of the chain must be allowed to have, or null when only its
     *                       {@code federation_entity} type matters, which is always allowed
     * @return true or false
     */
    public static boolean isSatisfiedBy(final TrustChain chain, @Nullable final EntityType leafEntityType) {
        if (!namesSigningKeys(chain)) {
            LOGGER.warn("Trust chain of [{}] has a statement that does not name the key it is signed with",
                chain.getLeafConfiguration().getEntityID());
            return false;
        }
        val entities = new ArrayList<EntityID>();
        entities.add(chain.getLeafConfiguration().getEntityID());
        val superiors = chain.getSuperiorStatements();
        for (var position = 0; position < superiors.size(); position++) {
            val statement = superiors.get(position);
            try {
                val constraints = statement.getClaimsSet().getJSONObjectClaim(CONSTRAINTS);
                if (constraints != null && !isSatisfiedBy(constraints, position, entities, leafEntityType)) {
                    LOGGER.warn("Trust chain of [{}] breaks the constraints set by [{}]", entities.getFirst(), statement.getClaimsSet().getIssuer());
                    return false;
                }
            } catch (final Exception e) {
                LOGGER.warn("Constraints set by [{}] cannot be read: [{}]", statement.getClaimsSet().getIssuer(), e.getMessage());
                return false;
            }
            entities.add(statement.getClaimsSet().getIssuerEntityID());
        }
        return true;
    }

    private static boolean isSatisfiedBy(final Map<String, Object> constraints, final int intermediates,
                                         final List<EntityID> subordinates, @Nullable final EntityType leafEntityType) {
        val maxPathLength = constraints.get(MAX_PATH_LENGTH);
        if (maxPathLength != null && !allowsPathLength(maxPathLength, intermediates)) {
            return false;
        }
        val allowedEntityTypes = constraints.get(ALLOWED_ENTITY_TYPES);
        if (allowedEntityTypes != null && (!(allowedEntityTypes instanceof final List<?> types)
            || (leafEntityType != null && !types.contains(leafEntityType.getValue())))) {
            return false;
        }
        val namingConstraints = constraints.get(NAMING_CONSTRAINTS);
        if (namingConstraints == null) {
            return true;
        }
        if (!(namingConstraints instanceof final Map<?, ?> naming)) {
            return false;
        }
        val permitted = readNames(naming.get("permitted"));
        val excluded = readNames(naming.get("excluded"));
        return subordinates.stream().allMatch(entity -> {
            val host = StringUtils.lowerCase(URI.create(entity.getValue()).getHost());
            return host != null
                && excluded.stream().noneMatch(name -> matchesDomainName(host, name))
                && (permitted.isEmpty() || permitted.stream().anyMatch(name -> matchesDomainName(host, name)));
        });
    }

    /**
     * Whether {@code max_path_length}, which must be a non-negative integer, allows the given number of intermediates.
     * A fraction, a negative number or anything else is refused rather than rounded, and a number too large for an
     * {@code int} is compared as it is.
     *
     * @param maxPathLength the constraint value
     * @param intermediates the number of intermediates
     * @return true or false
     */
    private static boolean allowsPathLength(final Object maxPathLength, final int intermediates) {
        if (!(maxPathLength instanceof final Number number)) {
            return false;
        }
        try {
            val limit = new BigDecimal(number.toString()).toBigIntegerExact();
            return limit.signum() >= 0 && limit.compareTo(BigInteger.valueOf(intermediates)) >= 0;
        } catch (final ArithmeticException | NumberFormatException e) {
            LOGGER.debug("max_path_length [{}] is not an integer: [{}]", maxPathLength, e.getMessage());
            return false;
        }
    }

    private static List<String> readNames(@Nullable final Object names) {
        if (names == null) {
            return List.of();
        }
        if (!(names instanceof final List<?> list) || !list.stream().allMatch(String.class::isInstance)) {
            throw new IllegalArgumentException("Naming constraints must be lists of domain names");
        }
        return list.stream().map(String.class::cast).map(StringUtils::lowerCase).toList();
    }

    private static boolean matchesDomainName(final String host, final String name) {
        return Strings.CS.startsWith(name, ".") ? Strings.CS.endsWith(host, name) : host.equals(name);
    }

    private static boolean namesSigningKeys(final TrustChain chain) {
        val statements = new ArrayList<EntityStatement>();
        statements.add(chain.getLeafConfiguration());
        statements.addAll(chain.getSuperiorStatements());
        if (chain.getTrustAnchorConfiguration() != null) {
            statements.add(chain.getTrustAnchorConfiguration());
        }
        return statements.stream().allMatch(statement -> StringUtils.isNotBlank(statement.getSignedStatement().getHeader().getKeyID()));
    }
}
