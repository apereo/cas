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

    @Override
    protected List<Object> getAttributeValue(final Map<String, List<Object>> resolvedAttributes, final String attributeName, final String mappedAttributeName) {
        return Objects.requireNonNullElseGet(resolvedAttributes.get(mappedAttributeName), List::of);
    }

    @Override
    protected AttributeMappingRequest buildAttributeMappingRequest(final Map<String, List<Object>> resolvedAttributes, final String attributeName,
                                                                   final String mappedAttributeName, final List<Object> attributeValue) {
        val request = super.buildAttributeMappingRequest(resolvedAttributes, attributeName, mappedAttributeName, attributeValue);
        return request.withMappedAttributeName(attributeName);
    }
}
