package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;

import java.util.List;

/**
 * This is {@link OidcSimpleIdTokenClaimCollector}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcSimpleIdTokenClaimCollector implements OidcIdTokenClaimCollector {
    private final AttributeDefinitionStore attributeDefinitionStore;

    @Override
    public void collect(final JwtClaims claims, final String name,
                        final List<Object> values) {
        if (!values.isEmpty()) {
            val result = attributeDefinitionStore.locateAttributeDefinition(name, OidcAttributeDefinition.class);
            val finalValue = result.map(defn -> defn.isSingleValue() && values.size() == 1 ? values.get(0) : values)
                .orElseGet(() -> values.size() == 1 ? values.get(0) : values);
            LOGGER.debug("Collecting ID token claim [{}] with value(s) [{}]", name, finalValue);
            claims.setClaim(name, finalValue);
        }
    }
}

