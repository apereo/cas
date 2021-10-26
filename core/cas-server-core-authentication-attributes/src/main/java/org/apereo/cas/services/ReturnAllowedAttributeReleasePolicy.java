package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ReturnAllowedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -5771481877391140569L;

    private List<String> allowedAttributes = new ArrayList<>(0);

    @Override
    public Map<String, List<Object>> getAttributesInternal(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) {
        return authorizeReleaseOfAllowedAttributes(context, attributes);
    }

    /**
     * Authorize release of allowed attributes map.
     *
     * @param context    the context
     * @param attributes the attributes
     * @return the map
     */
    protected Map<String, List<Object>> authorizeReleaseOfAllowedAttributes(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);
        val attributesToRelease = new HashMap<String, List<Object>>();
        getAllowedAttributes()
            .stream()
            .filter(resolvedAttributes::containsKey)
            .forEach(attr -> {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes", attr);
                attributesToRelease.put(attr, resolvedAttributes.get(attr));
            });
        return attributesToRelease;
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        return getAllowedAttributes();
    }
}
