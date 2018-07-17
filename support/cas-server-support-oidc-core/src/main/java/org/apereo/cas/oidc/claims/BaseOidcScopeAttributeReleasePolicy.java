package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.claims.mapping.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

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
public abstract class BaseOidcScopeAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -7302163334687300920L;

    private List<String> allowedAttributes;

    @JsonIgnore
    private String scopeName;

    public BaseOidcScopeAttributeReleasePolicy(final String scopeName) {
        this.scopeName = scopeName;
    }

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attributes, final RegisteredService service) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        if (applicationContext == null) {
            LOGGER.warn("Could not locate the application context to process attributes");
            return new HashMap<>();
        }
        val resolvedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);
        val attributesToRelease = Maps.<String, Object>newHashMapWithExpectedSize(attributes.size());
        LOGGER.debug("Attempting to map and filter claims based on resolved attributes [{}]", resolvedAttributes);
        val properties = applicationContext.getBean(CasConfigurationProperties.class);
        val supportedClaims = properties.getAuthn().getOidc().getClaims();
        val allowedClaims = new LinkedHashSet<String>(getAllowedAttributes());
        allowedClaims.retainAll(supportedClaims);
        LOGGER.debug("[{}] is designed to allow claims [{}] for scope [{}]. After cross-checking with "
                + "supported claims [{}], the final collection of allowed attributes is [{}]", getClass().getSimpleName(),
            getAllowedAttributes(), getScopeName(), supportedClaims, allowedClaims);
        allowedClaims.stream()
            .map(claim -> mapClaimToAttribute(claim, resolvedAttributes))
            .filter(p -> p.getValue() != null)
            .forEach(p -> attributesToRelease.put(p.getKey(), p.getValue()));
        return attributesToRelease;
    }

    private Pair<String, Object> mapClaimToAttribute(final String claim, final Map<String, Object> resolvedAttributes) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val attributeToScopeClaimMapper =
            applicationContext.getBean("oidcAttributeToScopeClaimMapper", OidcAttributeToScopeClaimMapper.class);
        LOGGER.debug("Attempting to process claim [{}]", claim);
        if (attributeToScopeClaimMapper.containsMappedAttribute(claim)) {
            val mappedAttr = attributeToScopeClaimMapper.getMappedAttribute(claim);
            val value = resolvedAttributes.get(mappedAttr);
            LOGGER.debug("Found mapped attribute [{}] with value [{}] for claim [{}]", mappedAttr, value, claim);
            return Pair.of(claim, value);
        }
        val value = resolvedAttributes.get(claim);
        LOGGER.debug("No mapped attribute is defined for claim [{}]; Used [{}] to locate value [{}]", claim, claim, value);
        return Pair.of(claim, value);
    }
}
