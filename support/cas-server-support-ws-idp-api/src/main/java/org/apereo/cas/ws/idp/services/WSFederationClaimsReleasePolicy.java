package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ws.idp.WSFederationClaims;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link WSFederationClaimsReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
public class WSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -2814928645221579489L;

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

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attrs, final RegisteredService service) {
        val resolvedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = Maps.<String, Object>newHashMapWithExpectedSize(resolvedAttributes.size());
        getAllowedAttributes().entrySet().stream().filter(entry -> WSFederationClaims.contains(entry.getKey().toUpperCase())).forEach(entry -> {
            val claimName = entry.getKey();
            val attributeName = entry.getValue();
            val claim = WSFederationClaims.valueOf(claimName.toUpperCase());
            LOGGER.debug("Evaluating claimName [{}] mapped to attribute name [{}]", claim.getUri(), attributeName);
            val value = resolvedAttributes.get(attributeName);
            if (value != null) {
                LOGGER.debug("Adding claimName [{}] to the collection of released attributes", claim.getUri());
                attributesToRelease.put(claim.getUri(), value);
            }
        });
        return attributesToRelease;
    }
}
