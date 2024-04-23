package org.apereo.cas.oidc.claims;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceChainingAttributeReleasePolicy;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OidcDefaultAttributeToScopeClaimMapper}.
 * In order for attributes to be released when using a scope in OIDC, attributes need to mapped
 * in to the correct OIDC name (i.e. {@code given_name}) for those attributes.
 * The side effect is that the attribute will always be released as the mapped name
 * even for non-OIDC requests.
 * This component allows for an arbitrary mapping between the predefined OIDC claims and attributes.
 * If a mapping is found for a claim, then the attribute mapped to the claim will be used.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OidcDefaultAttributeToScopeClaimMapper implements OidcAttributeToScopeClaimMapper {

    /**
     * Map OIDC claim to an attribute.
     */
    private final Map<String, String> claimsToAttribute;

    @Override
    public String getMappedAttribute(final String claim, final RegisteredService registeredService) {
        val container = getClaimMappingsContainer(registeredService);
        return Optional.ofNullable(container.get(claim)).orElseGet(() -> claimsToAttribute.get(claim));
    }

    @Override
    public boolean containsMappedAttribute(final String claim, final RegisteredService registeredService) {
        val container = getClaimMappingsContainer(registeredService);
        return claimsToAttribute.containsKey(claim) || container.containsKey(claim);
    }

    private static Map<String, String> getClaimMappingsContainer(final RegisteredService registeredService) {
        var finalMap = Stream.<Map.Entry<String, String>>empty();
        if (registeredService.getAttributeReleasePolicy() instanceof final RegisteredServiceChainingAttributeReleasePolicy chain) {
            for (val policy : chain.getPolicies()) {
                val result = getClaimMappingsForPolicy(policy);
                finalMap = Stream.concat(finalMap, result);
            }
        }
        val result = getClaimMappingsForPolicy(registeredService.getAttributeReleasePolicy());
        val finalResult = Stream.concat(finalMap, result);
        return finalResult.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Stream<Map.Entry<String, String>> getClaimMappingsForPolicy(final RegisteredServiceAttributeReleasePolicy policy) {
        return policy instanceof OidcRegisteredServiceAttributeReleasePolicy
            ? ((OidcRegisteredServiceAttributeReleasePolicy) policy).getClaimMappings().entrySet().stream()
            : Stream.empty();
    }
}
