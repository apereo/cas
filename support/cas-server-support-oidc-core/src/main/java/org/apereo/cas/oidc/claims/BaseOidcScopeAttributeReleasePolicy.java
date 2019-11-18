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
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseOidcScopeAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -7302163334687300920L;

    private List<String> allowedAttributes;

    @JsonIgnore
    private String scopeType;

    public BaseOidcScopeAttributeReleasePolicy(final String scopeType) {
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
        val supportedClaims = properties.getAuthn().getOidc().getClaims();
        
        val allowedClaims = new LinkedHashSet<String>(getAllowedAttributes());
        allowedClaims.retainAll(supportedClaims);
        LOGGER.debug("[{}] is designed to allow claims [{}] for scope [{}]. After cross-checking with "
                + "supported claims [{}], the final collection of allowed attributes is [{}]", getClass().getSimpleName(),
            getAllowedAttributes(), getScopeType(), supportedClaims, allowedClaims);
        allowedClaims.stream()
            .map(claim -> mapClaimToAttribute(claim, resolvedAttributes))
            .filter(p -> p.getValue() != null)
            .forEach(p -> attributesToRelease.put(p.getKey(), CollectionUtils.toCollection(p.getValue(), ArrayList.class)));
        return attributesToRelease;
    }

    private static Pair<String, Object> mapClaimToAttribute(final String claim, final Map<String, List<Object>> resolvedAttributes) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val attributeToScopeClaimMapper =
            applicationContext.getBean("oidcAttributeToScopeClaimMapper", OidcAttributeToScopeClaimMapper.class);
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
}
