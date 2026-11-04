package org.apereo.cas.oidc.federation.chain;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
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
     * Whether the trust chain satisfies the constraints of each of its subordinate statements.
     *
     * @param chain          the trust chain
     * @param leafEntityType the entity type the subject of the chain must be allowed to have, or null when only its
     *                       {@code federation_entity} type matters, which is always allowed
     * @return true or false
     */
    public static boolean isSatisfiedBy(final TrustChain chain, @Nullable final EntityType leafEntityType) {
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
        if (maxPathLength != null && (!(maxPathLength instanceof final Number number) || number.intValue() < 0 || intermediates > number.intValue())) {
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
}
