package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.oidc.assurance.AssuranceVerifiedClaimsProducer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.JsonUtils;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    protected final AssuranceVerifiedClaimsProducer assuranceVerifiedClaimsProducer;

    @Override
    public void conclude(final JwtClaims claims) {
        val claimNames = Set.copyOf(claims.getClaimNames());
        claimNames.forEach(claimName ->
            attributeDefinitionStore.locateAttributeDefinition(claimName, OidcAttributeDefinition.class)
                .or(() -> attributeDefinitionStore.locateAttributeDefinitionByName(claimName, OidcAttributeDefinition.class))
                .stream()
                .filter(definition -> StringUtils.isNotBlank(definition.getTrustFramework()))
                .findFirst()
                .ifPresent(definition -> assuranceVerifiedClaimsProducer.produce(claims, claimName, definition.getTrustFramework())));
    }

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
                    () -> {
                        val claimName = attributeDefinition.map(DefaultAttributeDefinition::getName).orElse(name);
                        collectClaim(jwtClaims, claimName, finalValue);
                    });
        }
    }

    protected void collectClaim(final JwtClaims claims, final String name, final Object finalValue) {
        LOGGER.debug("Collecting ID token claim [{}] with value(s) [{}]", name, finalValue);
        if (JsonUtils.isValidJsonObject(finalValue.toString())) {
            val jsonValue = JsonUtils.parse(finalValue.toString(), Map.class);
            claims.setClaim(name, jsonValue);
        } else {
            claims.setClaim(name, finalValue);
        }
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

