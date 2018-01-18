package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;

/**
 * Return only the collection of allowed attributes out of what's resolved
 * for the principal.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReturnAllowedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -5771481877391140569L;

    private List<String> allowedAttributes = new ArrayList<>();

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attrs, final RegisteredService service) {
        return authorizeReleaseOfAllowedAttributes(attrs);
    }

    /**
     * Authorize release of allowed attributes map.
     *
     * @param attrs the attributes
     * @return the map
     */
    protected Map<String, Object> authorizeReleaseOfAllowedAttributes(final Map<String, Object> attrs) {
        final Map<String, Object> resolvedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        final Map<String, Object> attributesToRelease = new HashMap<>(resolvedAttributes.size());
        getAllowedAttributes().stream().map(attr -> new Object[]{attr, resolvedAttributes.get(attr)}).filter(pair -> pair[1] != null)
            .forEach(attribute -> {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes", attribute[0]);
                attributesToRelease.put((String) attribute[0], attribute[1]);
            });
        return attributesToRelease;
    }

}
