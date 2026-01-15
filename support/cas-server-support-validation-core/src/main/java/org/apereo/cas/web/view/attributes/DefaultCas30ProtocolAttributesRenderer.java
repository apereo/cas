package org.apereo.cas.web.view.attributes;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;

/**
 * This is {@link DefaultCas30ProtocolAttributesRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultCas30ProtocolAttributesRenderer implements CasProtocolAttributesRenderer {

    @Override
    public Collection<String> render(final Map<String, Object> attributes) {
        val formattedAttributes = new ArrayList<String>(attributes.size());
        LOGGER.trace("Beginning to format/render attributes for the response");
        attributes.forEach((attributeName, v) -> {
            val values = CollectionUtils.toCollection(v);
            values.forEach(value -> {
                val name = CasProtocolAttributesRenderer.sanitizeAttributeName(attributeName);
                val fmt = buildSingleAttributeDefinitionLine(name, value);
                LOGGER.trace("Formatted attribute for the response: [{}]", fmt);
                formattedAttributes.add(fmt);
            });
        });
        return formattedAttributes;
    }

    protected String buildSingleAttributeDefinitionLine(final String attributeName, final Object value) {
        return "<cas:%s>%s</cas:%s>".formatted(attributeName, encodeAttributeValue(value), attributeName);
    }

    protected String encodeAttributeValue(final Object value) {
        return StringEscapeUtils.escapeXml10(value.toString().trim());
    }
}
