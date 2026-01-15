package org.apereo.cas.ws.idp.services;

import module java.base;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.ws.idp.WSFederationConstants;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;

/**
 * This is {@link CustomNamespaceWSFederationClaimsReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class CustomNamespaceWSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -1723928645221579489L;

    private String namespace = WSFederationConstants.HTTP_SCHEMAS_APEREO_CAS;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, String> allowedAttributes = new HashMap<>();

    public CustomNamespaceWSFederationClaimsReleasePolicy(final Map<String, String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attributes) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);
        val attributesToRelease = new HashMap<String, List<Object>>(resolvedAttributes.size());
        getAllowedAttributes().forEach((key, value) -> {
            if (resolvedAttributes.containsKey(value)) {
                val attributeValue = resolvedAttributes.get(value);
                if (attributeValue != null) {
                    val claimName = Strings.CI.appendIfMissing(this.namespace, "/") + key;
                    LOGGER.debug("Adding claim name [{}] with value [{}]", claimName, attributeValue);
                    attributesToRelease.put(claimName, attributeValue);
                }
            }
        });
        return attributesToRelease;
    }

    @Override
    public List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        return new ArrayList<>(getAllowedAttributes().keySet());
    }
}
