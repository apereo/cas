package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link CustomWSFederationClaimsReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@Setter
public class CustomWSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -1814928645221579489L;

    private Map<String, String> allowedAttributes;

    private String namespace;

    /**
     * Instantiates a new Return allowed attribute release policy.
     */
    public CustomWSFederationClaimsReleasePolicy() {
        this(new HashMap<>());
    }

    /**
     * Instantiates a new Return allowed attribute release policy.
     *
     * @param allowedAttributes the allowed attributes
     */
    public CustomWSFederationClaimsReleasePolicy(final Map<String, String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attrs, final RegisteredService service) {
        final Map<String, Object> resolvedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        final Map<String, Object> attributesToRelease = Maps.newHashMapWithExpectedSize(resolvedAttributes.size());

        getAllowedAttributes().forEach((key, claimValue) -> {
            if (resolvedAttributes.containsKey(claimValue)) {
                final Object value = resolvedAttributes.get(claimValue);
                final String attributeName = namespace + key;
                LOGGER.debug("Loading custom claim attribute [{}] and value [{}]", attributeName, value);
                attributesToRelease.put(attributeName, value);
            }
        });

        return attributesToRelease;
    }
}
