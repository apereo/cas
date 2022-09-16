package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;

import lombok.RequiredArgsConstructor;
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
public class OidcSimpleIdTokenClaimCollector implements OidcIdTokenClaimCollector {
    private final AttributeDefinitionStore attributeDefinitionStore;

    @Override
    public void collect(final JwtClaims claims, final String name, final List<Object> values) {
        if (values.size() == 1) {
            val result = attributeDefinitionStore.locateAttributeDefinition(name, OidcAttributeDefinition.class);
            result.ifPresentOrElse(defn -> {
                if (defn.isSingleValue()) {
                    claims.setClaim(name, values.get(0));
                } else {
                    claims.setClaim(name, values);
                }
            }, () -> claims.setClaim(name, values));
        } else if (values.size() > 1) {
            claims.setClaim(name, values);
        }
    }
}

