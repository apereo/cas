package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.AttributeMappingRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * Return a collection of allowed attributes for the principal, but additionally,
 * offers the ability to resolve those attributes from a set attributes that provide the value.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ToString(callSuper = true)
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ReturnLinkedAttributeReleasePolicy extends BaseMappedAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = 6249488544304439150L;

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attrs) {
        return authorizeMappedAttributes(context, attrs);
    }

    /**
     * Each allowed attribute is sourced from the first linked attribute that produces a value;
     * linked attributes listed after it are ignored.
     *
     * @param attributesToRelease the attributes to release
     * @param mappingResults      the mapped attributes
     */
    @Override
    protected void collectMappedAttributes(final Map<String, List<Object>> attributesToRelease,
                                           final Map<String, List<Object>> mappingResults) {
        mappingResults.forEach((name, values) -> {
            if (values != null && !values.isEmpty()) {
                attributesToRelease.putIfAbsent(name, values);
            }
        });
    }

    @Override
    protected List<Object> getAttributeValue(final Map<String, List<Object>> resolvedAttributes, final String attributeName, final String mappedAttributeName) {
        return Objects.requireNonNullElseGet(resolvedAttributes.get(mappedAttributeName), List::of);
    }

    /**
     * Build the mapping request for one linked attribute. A linked attribute without a value produces nothing:
     * the released attribute's own value, if the principal happens to have one, is not used in its place.
     *
     * @param resolvedAttributes  the resolved attributes
     * @param attributeName       the released attribute
     * @param mappedAttributeName the linked attribute
     * @param attributeValue      the value of the linked attribute
     * @return the mapping request
     */
    @Override
    protected AttributeMappingRequest buildAttributeMappingRequest(final Map<String, List<Object>> resolvedAttributes, final String attributeName,
                                                                   final String mappedAttributeName, final List<Object> attributeValue) {
        val sourceAttributes = attributeValue.isEmpty() ? withoutAttribute(resolvedAttributes, attributeName) : resolvedAttributes;
        val request = super.buildAttributeMappingRequest(sourceAttributes, attributeName, mappedAttributeName, attributeValue);
        return request.withMappedAttributeName(attributeName);
    }

    private static Map<String, List<Object>> withoutAttribute(final Map<String, List<Object>> attributes, final String attributeName) {
        val result = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(attributes);
        result.remove(attributeName);
        return result;
    }
}
