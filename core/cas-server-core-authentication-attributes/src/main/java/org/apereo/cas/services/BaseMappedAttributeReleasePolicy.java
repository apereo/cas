package org.apereo.cas.services;

import org.apereo.cas.authentication.AttributeMappingRequest;
import org.apereo.cas.authentication.PrincipalAttributesMapper;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Return a collection of allowed attributes for the principal, but additionally,
 * offers the ability to rename attributes on a per-service level.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class BaseMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -6249488544306639050L;

    @JsonProperty("allowedAttributes")
    private Map<String, Object> allowedAttributes = new TreeMap<>();

    /**
     * Gets the allowed attributes.
     *
     * @return the allowed attributes
     */
    public Map<String, Object> getAllowedAttributes() {
        return new TreeMap<>(this.allowedAttributes);
    }

    /**
     * Authorize release of allowed attributes map.
     * Map each entry in the allowed list into an array first
     * by the original key, value and the original entry itself.
     * Then process the array to populate the map for allowed attributes.
     *
     * @param context    the context
     * @param attributes the attributes
     * @return the map
     */
    protected Map<String, List<Object>> authorizeMappedAttributes(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) {

        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);
        resolvedAttributes.putAll(context.getReleasingAttributes());

        val attributesToRelease = new HashMap<String, List<Object>>();
        getAllowedAttributes().forEach((attributeName, value) -> {
            val mappedAttributes = determineMappedAttributes(value);

            LOGGER.trace("Attempting to map allowed attribute name [{}]", attributeName);
            mappedAttributes.forEach(mapped -> {
                val mappedAttributeName = mapped.toString();
                val attributeValue = getAttributeValue(resolvedAttributes, attributeName, mappedAttributeName);
                LOGGER.debug("Mapping attribute [{}] to [{}] with value [{}]",
                    attributeName, mappedAttributeName, attributeValue);
                val mappingRequest = buildAttributeMappingRequest(resolvedAttributes, attributeName, mappedAttributeName, attributeValue);
                val mappingResults = PrincipalAttributesMapper.defaultMapper().map(mappingRequest);
                attributesToRelease.putAll(mappingResults);
            });
        });
        return attributesToRelease;
    }

    protected AttributeMappingRequest buildAttributeMappingRequest(final Map<String, List<Object>> resolvedAttributes,
                                                                   final String attributeName, final String mappedAttributeName,
                                                                   final List<Object> attributeValue) {
        return AttributeMappingRequest.builder()
            .attributeName(attributeName)
            .mappedAttributeName(mappedAttributeName)
            .attributeValue(attributeValue)
            .resolvedAttributes(resolvedAttributes)
            .build();
    }

    protected List<Object> getAttributeValue(final Map<String, List<Object>> resolvedAttributes,
                                             final String attributeName, final String mappedAttributeName) {
        return resolvedAttributes.get(attributeName);
    }

    protected List<Object> determineMappedAttributes(final Object value) {
        return CollectionUtils.wrap(value);
    }
}
