package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.oidc.assurance.AssuranceVerificationSource;
import org.apereo.cas.util.CollectionUtils;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import java.util.HashMap;
import java.util.List;

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
    public void collect(final JwtClaims claims, final String name, final List<Object> values) {
        if (!values.isEmpty()) {
            val result = attributeDefinitionStore.locateAttributeDefinition(name, OidcAttributeDefinition.class)
                .or(() -> attributeDefinitionStore.locateAttributeDefinitionByName(name, OidcAttributeDefinition.class));
            val finalValue = result.map(definition -> definition.toAttributeValue(values))
                .orElseGet(() -> values.size() == 1 ? values.getFirst() : values);

            if (result.isPresent() && result.get().isStructured() && result.get().getName().contains(".")) {
                val claimNames = Splitter.on('.').splitToList(result.get().getName().trim());
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
            } else {
                LOGGER.debug("Collecting ID token claim [{}] with value(s) [{}]", name, finalValue);
                claims.setClaim(name, finalValue);
            }
        }
    }
}

