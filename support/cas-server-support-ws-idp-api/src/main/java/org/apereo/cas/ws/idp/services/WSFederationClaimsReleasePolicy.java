package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link WSFederationClaimsReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class WSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    private static final long serialVersionUID = -2814928645221579489L;

    private static final Logger LOGGER = LoggerFactory.getLogger(WSFederationClaimsReleasePolicy.class);

    private Map<String, String> allowedAttributes;

    /**
     * Instantiates a new Return allowed attribute release policy.
     */
    public WSFederationClaimsReleasePolicy() {
        this(new HashMap<>());
    }

    /**
     * Instantiates a new Return allowed attribute release policy.
     *
     * @param allowedAttributes the allowed attributes
     */
    public WSFederationClaimsReleasePolicy(final Map<String, String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    public void setAllowedAttributes(final Map<String, String> allowed) {
        this.allowedAttributes = allowed;
    }

    public Map<String, String> getAllowedAttributes() {
        return this.allowedAttributes;
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal,
                                                        final Map<String, Object> attrs, final RegisteredService service) {
        final Map<String, Object> resolvedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        final Map<String, Object> attributesToRelease = new HashMap<>(resolvedAttributes.size());
        getAllowedAttributes()
                .entrySet()
                .stream()
                .filter(entry -> WSFederationClaims.contains(entry.getKey().toUpperCase()))
                .forEach(entry -> {
                    final String claimName = entry.getKey();
                    final String attributeName = entry.getValue();
                    
                    final WSFederationClaims claim = WSFederationClaims.valueOf(claimName.toUpperCase());
                    LOGGER.debug("Evaluating claimName [{}] mapped to attribute name [{}]", claim.getUri(), attributeName);
                    final Object value = resolvedAttributes.get(attributeName);
                    if (value != null) {
                        LOGGER.debug("Adding claimName [{}] to the collection of released attributes", claim.getUri());
                        attributesToRelease.put(claim.getUri(), value);
                    }
                });
        return attributesToRelease;

    }
}
