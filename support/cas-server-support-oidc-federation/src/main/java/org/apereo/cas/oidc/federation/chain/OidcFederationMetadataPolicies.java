package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
import com.nimbusds.openid.connect.sdk.federation.policy.MetadataPolicy;
import com.nimbusds.openid.connect.sdk.federation.policy.language.PolicyViolationException;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Metadata policies of OpenID Federation 1.0, section 6.1, as they apply to the subject of a trust chain. The metadata of
 * the subject's entity configuration is first overlaid with the {@code metadata} its immediate superior asserts; the
 * {@code metadata_policy} of every subordinate statement is then validated, merged from the most superior statement down
 * to the immediate superior, and applied. Only the standard operators are understood: another operator is ignored, unless
 * a statement of the chain lists it in {@code metadata_policy_crit}, which makes it a policy error, as does a value of
 * {@code metadata_policy_crit} that is not a list of strings. The {@code scope}
 * parameter, a space-separated string, is handled as a list of values by the operators. Any policy error invalidates the
 * trust chain.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@UtilityClass
public class OidcFederationMetadataPolicies {
    /**
     * The standard metadata policy operators (section 6.1.3.1).
     */
    public static final Set<String> STANDARD_OPERATORS = Set.of("value", "add", "default", "one_of", "subset_of", "superset_of", "essential");

    /**
     * The claim of a subordinate statement that lists the metadata policy operators that must be understood; nimbus still
     * reads it under its earlier name, {@code policy_language_crit}.
     */
    public static final String METADATA_POLICY_CRIT = "metadata_policy_crit";

    private static final String METADATA = "metadata";

    private static final String SCOPE = "scope";

    /**
     * Resolve the metadata of the given entity type of the subject of the trust chain. The subject must publish metadata
     * of that type in its entity configuration: the metadata its superiors assert, and their policies, only apply to the
     * entity types the subject has (section 3), so they never give it a type it does not claim.
     *
     * @param chain      the trust chain
     * @param entityType the entity type
     * @return the resolved metadata
     * @throws Exception when the subject has no metadata of the type, or a policy error makes the trust chain invalid
     */
    public static JSONObject resolveMetadata(final TrustChain chain, final EntityType entityType) throws Exception {
        val leafMetadata = chain.getLeafConfiguration().getSignedStatement().getJWTClaimsSet().getClaim(METADATA);
        if (!(leafMetadata instanceof final Map<?, ?> entityTypes && entityTypes.get(entityType.getValue()) instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Entity " + chain.getLeafConfiguration().getEntityID()
                + " publishes no metadata of type " + entityType);
        }
        val metadata = new JSONObject();
        metadata.putAll(readEntityTypeObject(leafMetadata, entityType));
        val superiors = chain.getSuperiorStatements();
        if (!superiors.isEmpty()) {
            metadata.putAll(readEntityTypeObject(superiors.getFirst().getSignedStatement().getJWTClaimsSet().getClaim(METADATA), entityType));
        }
        val critical = new HashSet<String>();
        for (val statement : superiors) {
            FunctionUtils.doIfNotNull(statement.getSignedStatement().getJWTClaimsSet().getStringListClaim(METADATA_POLICY_CRIT), critical::addAll);
        }
        val policies = new ArrayList<MetadataPolicy>();
        for (val statement : superiors.reversed()) {
            val policy = readPolicy(statement, entityType, critical);
            if (policy != null) {
                policies.add(policy);
            }
        }
        if (policies.isEmpty()) {
            return metadata;
        }
        val scope = metadata.get(SCOPE);
        if (scope instanceof final String value) {
            metadata.put(SCOPE, splitScope(value));
        }
        val resolved = MetadataPolicy.combine(policies).apply(metadata);
        if (resolved.get(SCOPE) instanceof final List<?> values) {
            resolved.put(SCOPE, values.stream().map(String::valueOf).collect(Collectors.joining(" ")));
        }
        return resolved;
    }

    /**
     * Validate a metadata policy, as published in a subordinate statement, with the standard operators: operators that are
     * not standard are left to the parties that understand them.
     *
     * @param metadataPolicy the metadata policy, by entity type
     * @throws Exception when the policy is invalid
     */
    public static void validate(final Map<String, Object> metadataPolicy) throws Exception {
        for (val entry : metadataPolicy.entrySet()) {
            if (!(entry.getValue() instanceof final Map<?, ?> entityTypePolicy)) {
                throw new PolicyViolationException("Metadata policy of entity type " + entry.getKey() + " is not a JSON object");
            }
            MetadataPolicy.parse(sanitize(entityTypePolicy, Set.of(), false));
        }
    }

    private static @Nullable MetadataPolicy readPolicy(final EntityStatement statement, final EntityType entityType,
                                             final Set<String> critical)
            throws Exception {
        val metadataPolicy = statement.getClaimsSet().getMetadataPolicyJSONObject();
        if (metadataPolicy == null || metadataPolicy.get(entityType.getValue()) == null) {
            return null;
        }
        if (!(metadataPolicy.get(entityType.getValue()) instanceof final Map<?, ?> entityTypePolicy)) {
            throw new PolicyViolationException("Metadata policy of " + statement.getClaimsSet().getIssuer() + " is not a JSON object");
        }
        return MetadataPolicy.parse(sanitize(entityTypePolicy, critical, true));
    }

    private static JSONObject sanitize(final Map<?, ?> entityTypePolicy, final Set<String> critical, final boolean refuseCritical)
            throws PolicyViolationException {
        val sanitized = new JSONObject();
        for (val parameter : entityTypePolicy.entrySet()) {
            if (!(parameter.getValue() instanceof final Map<?, ?> operators)) {
                throw new PolicyViolationException("Policy of metadata parameter " + parameter.getKey() + " is not a JSON object");
            }
            val understood = new JSONObject();
            for (val operator : operators.entrySet()) {
                val name = String.valueOf(operator.getKey());
                if (STANDARD_OPERATORS.contains(name)) {
                    understood.put(name, SCOPE.equals(parameter.getKey()) && operator.getValue() instanceof final String value
                        ? splitScope(value) : operator.getValue());
                } else if (refuseCritical && critical.contains(name)) {
                    throw new PolicyViolationException("Critical metadata policy operator " + name + " is not supported");
                } else {
                    LOGGER.debug("Metadata policy operator [{}] of parameter [{}] is not understood and is ignored", name, parameter.getKey());
                }
            }
            sanitized.put(String.valueOf(parameter.getKey()), understood);
        }
        return sanitized;
    }

    private static Map<String, Object> readEntityTypeObject(final Object metadata, final EntityType entityType) {
        if (metadata instanceof final Map<?, ?> entityTypes && entityTypes.get(entityType.getValue()) instanceof final Map<?, ?> values) {
            val result = new LinkedHashMap<String, Object>();
            values.forEach((name, value) -> result.put(String.valueOf(name), value));
            return result;
        }
        return Map.of();
    }

    private static List<String> splitScope(final String scope) {
        return Arrays.stream(StringUtils.split(scope)).toList();
    }
}
