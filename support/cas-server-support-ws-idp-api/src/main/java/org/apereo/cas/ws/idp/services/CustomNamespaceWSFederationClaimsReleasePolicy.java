package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ws.idp.WSFederationConstants;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link CustomNamespaceWSFederationClaimsReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@Setter
public class CustomNamespaceWSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -1723928645221579489L;

    private String namespace = WSFederationConstants.HTTP_SCHEMAS_APEREO_CAS;

    private Map<String, String> allowedAttributes;

    public CustomNamespaceWSFederationClaimsReleasePolicy() {
        this(new HashMap<>());
    }

    public CustomNamespaceWSFederationClaimsReleasePolicy(final Map<String, String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attrs, final RegisteredService service) {
        val resolvedAttributes = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = Maps.<String, Object>newHashMapWithExpectedSize(resolvedAttributes.size());
        getAllowedAttributes().forEach((key, value) -> {
            if (resolvedAttributes.containsKey(value)) {
                val attributeValue = resolvedAttributes.get(value);
                if (attributeValue != null) {
                    val claimName = StringUtils.appendIfMissing(this.namespace, "/") + key;
                    LOGGER.debug("Adding claim name [{}] with value [{}]", claimName, attributeValue);
                    attributesToRelease.put(claimName, attributeValue);
                }
            }
        });
        return attributesToRelease;
    }
}
