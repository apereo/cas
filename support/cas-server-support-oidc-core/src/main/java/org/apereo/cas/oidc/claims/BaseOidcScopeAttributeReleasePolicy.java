package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.claims.mapping.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * This is {@link BaseOidcScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class BaseOidcScopeAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -7302163334687300920L;

    @JsonProperty
    private List<String> allowedNormalClaims;

    @JsonProperty
    private Map<String, List<String>> allowedAggregatedClaims;

    @JsonIgnore
    private String scopeType;

    protected BaseOidcScopeAttributeReleasePolicy(final String scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attributes,
                                                           final RegisteredService registeredService, final Service selectedService) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        if (applicationContext == null) {
            LOGGER.warn("Could not locate the application context to process attributes");
            return new HashMap<>(0);
        }
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);

        val attributesToRelease = Maps.<String, List<Object>>newHashMapWithExpectedSize(attributes.size());
        LOGGER.debug("Attempting to map and filter claims based on resolved attributes [{}]", resolvedAttributes);

        val properties = applicationContext.getBean(CasConfigurationProperties.class);
        val supportedClaims = properties.getAuthn().getOidc().getDiscovery().getClaims();
        
        val allowedClaims = new LinkedHashSet<>(getAllowedNormalClaims());
        allowedClaims.addAll(getAllowedAggregatedClaims().keySet());
        allowedClaims.retainAll(supportedClaims);
        
        LOGGER.debug("[{}] is designed to allow claims [{}] for scope [{}]. After cross-checking with "
                + "supported claims [{}], the final collection of allowed attributes is [{}]", getClass().getSimpleName(),
                getAllowedNormalClaims(), getScopeType(), supportedClaims, allowedClaims);
        allowedClaims
                .forEach(claim -> {
                    if (getAllowedAggregatedClaims().containsKey(claim)) {
                        resolveValuesForTheClaimFieldsAndAddThem(resolvedAttributes, attributesToRelease, claim);
                    }
                    else {
                        resolveValuesForTheClaimAndAddThem(resolvedAttributes, attributesToRelease, claim);
                    }
                });
        return attributesToRelease;
    }

    private void resolveValuesForTheClaimAndAddThem(TreeMap<String, List<Object>> resolvedAttributes, HashMap<String, List<Object>> attributesToRelease, String claim) {
        Pair<String, Object> claimToAttribute = mapClaimToAttribute(claim, resolvedAttributes);
        if (claimToAttribute.getValue() != null) {
            attributesToRelease.put(claimToAttribute.getKey(), CollectionUtils.toCollection(claimToAttribute.getValue(), ArrayList.class));
        }
    }

    private void resolveValuesForTheClaimFieldsAndAddThem(TreeMap<String, List<Object>> resolvedAttributes, HashMap<String, List<Object>> attributesToRelease, String claim) {
        getAllowedAggregatedClaims().get(claim).stream()
                .filter(resolvedAttributes::containsKey)
                .forEach(field -> {
                    if (attributesToRelease.containsKey(claim)) {
                        val valueList = attributesToRelease.get(claim);
                        val map = (Map<String, List<Object>>)valueList.get(0);
                        map.put(field, resolvedAttributes.get(field));
                    }
                    else {
                        val fieldMap = new HashMap<String, List<Object>>();
                        fieldMap.put(field, resolvedAttributes.get(field));
                        attributesToRelease.put(claim, List.of(fieldMap));
                    }
                });
    }

    private static Pair<String, Object> mapClaimToAttribute(final String claim, final Map<String, List<Object>> resolvedAttributes) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val attributeToScopeClaimMapper =
            applicationContext.getBean(OidcAttributeToScopeClaimMapper.DEFAULT_BEAN_NAME, OidcAttributeToScopeClaimMapper.class);
        LOGGER.debug("Attempting to process claim [{}]", claim);
        if (attributeToScopeClaimMapper.containsMappedAttribute(claim)) {
            val mappedAttr = attributeToScopeClaimMapper.getMappedAttribute(claim);
            if (resolvedAttributes.containsKey(mappedAttr)) {
                val value = resolvedAttributes.get(mappedAttr);
                LOGGER.debug("Found mapped attribute [{}] with value [{}] for claim [{}]", mappedAttr, value, claim);
                return Pair.of(claim, value);
            } else {
                LOGGER.warn("Located claim [{}] mapped to attribute [{}], yet resolved attributes [{}] do not contain this attribute",
                    claim, mappedAttr, resolvedAttributes);
            }
        }
        val value = resolvedAttributes.get(claim);
        LOGGER.debug("No mapped attribute is defined for claim [{}]; Used [{}] to locate value [{}]", claim, claim, value);
        return Pair.of(claim, value);
    }

    @Override
    public List<String> determineRequestedAttributeDefinitions() {
        val attributes = new ArrayList<>(getAllowedNormalClaims());
        getAllowedAggregatedClaims().forEach((key, value) -> attributes.addAll(value));
        return attributes;
    }
}
