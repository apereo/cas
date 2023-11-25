package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.oidc.assurance.AssuranceVerificationSource;
import org.apereo.cas.util.CollectionUtils;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwt.JwtClaims;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link OidcSimpleIdTokenClaimCollector}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OidcSimpleIdTokenClaimCollector implements OidcIdTokenClaimCollector {
    protected final AttributeDefinitionStore attributeDefinitionStore;
    protected final AssuranceVerificationSource assuranceVerificationSource;

    @Override
    public void collect(final JwtClaims jwtClaims, final String name, final List<Object> values) {
        if (!values.isEmpty()) {
            val attributeDefinition = attributeDefinitionStore.locateAttributeDefinition(name, OidcAttributeDefinition.class)
                .or(() -> attributeDefinitionStore.locateAttributeDefinitionByName(name, OidcAttributeDefinition.class));
            val finalValue = attributeDefinition.map(definition -> definition.toAttributeValue(values))
                .orElseGet(() -> values.size() == 1 ? values.getFirst() : values);

            attributeDefinition
                .stream()
                .filter(OidcAttributeDefinition::isStructured)
                .filter(definition -> definition.getName().contains("."))
                .findFirst()
                .ifPresentOrElse(definition -> collectStructuredClaim(jwtClaims, definition, finalValue),
                    () -> collectClaim(jwtClaims, name, finalValue));

            attributeDefinition
                .stream()
                .filter(definition -> StringUtils.isNotBlank(definition.getTrustFramework()))
                .findFirst()
                .ifPresent(definition -> {
                    assuranceVerificationSource.findByTrustFramework(definition.getTrustFramework())
                        .ifPresent(Unchecked.consumer(verification -> {
                            val verifiedClaims = (Map) Objects.requireNonNullElseGet(jwtClaims.getClaimValue("verified_claims"), HashMap::new);
                            verifiedClaims.put("verification", JwtClaims.parse(verification.toJson()).getClaimsMap());
                            val claimName = StringUtils.defaultIfBlank(definition.getName(), name);
                            val currentClaimValue = jwtClaims.getClaimValue(claimName);
                            jwtClaims.unsetClaim(name);
                            val claims = (Map) Objects.requireNonNullElseGet(verifiedClaims.get("claims"), HashMap::new);
                            claims.put(claimName, currentClaimValue);
                            verifiedClaims.put("claims", claims);
                            collectClaim(jwtClaims, "verified_claims", verifiedClaims);
                        }));
                });
        }
    }

    protected void collectClaim(final JwtClaims claims, final String name, final Object finalValue) {
        LOGGER.debug("Collecting ID token claim [{}] with value(s) [{}]", name, finalValue);
        claims.setClaim(name, finalValue);
    }

    protected void collectStructuredClaim(final JwtClaims claims, final OidcAttributeDefinition definition,
                                          final Object finalValue) {
        val claimNames = Splitter.on('.').splitToList(definition.getName().trim());
        val structuredClaims = new HashMap<>();

        var lastClaimName = claimNames.getLast();
        structuredClaims.put(lastClaimName, finalValue);

        for (var i = claimNames.size() - 2; i >= 1; i--) {
            val lastClaimValue = structuredClaims.remove(lastClaimName);
            val currentClaimName = claimNames.get(i);
            structuredClaims.put(currentClaimName, CollectionUtils.wrap(lastClaimName, lastClaimValue));
            lastClaimName = currentClaimName;
        }
        LOGGER.debug("Collecting structured ID token claim [{}] with nested entries [{}]", claimNames.getFirst(), structuredClaims);
        claims.setClaim(claimNames.getFirst(), structuredClaims);
    }
}

