package org.apereo.cas.web.view.attributes;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link AttributeValuesPerLineProtocolAttributesRenderer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class AttributeValuesPerLineProtocolAttributesRenderer implements CasProtocolAttributesRenderer {
    @Override
    public Collection<String> render(final Map<String, Object> attributes) {
        val formattedAttributes = new ArrayList<String>(attributes.size());
        attributes.forEach((key, value) -> {
            val attributeValues = CollectionUtils.toCollection(value).stream().map(Object::toString).collect(Collectors.joining(","));
            formattedAttributes.add(key.concat("=").concat(attributeValues));
        });
        return formattedAttributes;
    }
}
