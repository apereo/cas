package org.apereo.cas.web.view.attributes;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        final List<String> formattedAttributes = new ArrayList<>(attributes.size());
        LOGGER.debug("Beginning to format/render attributes for the response");
        attributes.forEach((k, v) -> {
            final var values = CollectionUtils.toCollection(v);
            values.forEach(value -> {
                final var fmt = buildSingleAttributeDefinitionLine(k, value);
                LOGGER.debug("Formatted attribute for the response: [{}]", fmt);
                formattedAttributes.add(fmt);
            });
        });
        return formattedAttributes;
    }

    /**
     * Build single attribute definition line.
     *
     * @param attributeName the attribute name
     * @param value         the value
     * @return the string
     */
    protected String buildSingleAttributeDefinitionLine(final String attributeName, final Object value) {
        return new StringBuilder()
            .append("<cas:".concat(attributeName).concat(">"))
            .append(encodeAttributeValue(value))
            .append("</cas:".concat(attributeName).concat(">"))
            .toString();
    }

    /**
     * Encode attribute value.
     *
     * @param value the value
     * @return the string
     */
    protected String encodeAttributeValue(final Object value) {
        return StringEscapeUtils.escapeXml10(value.toString().trim());
    }
}
