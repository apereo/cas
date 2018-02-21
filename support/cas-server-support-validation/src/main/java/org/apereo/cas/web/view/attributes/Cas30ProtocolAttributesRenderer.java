package org.apereo.cas.web.view.attributes;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link Cas30ProtocolAttributesRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class Cas30ProtocolAttributesRenderer implements CasProtocolAttributesRenderer {

    @Override
    public Collection<String> render(final Map<String, Object> attributes) {
        final List<String> formattedAttributes = new ArrayList<>(attributes.size());
        LOGGER.debug("Beginning to format/render attributes for the response");
        attributes.forEach((k, v) -> {
            final Set<Object> values = CollectionUtils.toCollection(v);
            values.forEach(value -> {
                final String fmt = new StringBuilder()
                    .append("<cas:".concat(k).concat(">"))
                    .append(StringEscapeUtils.escapeXml10(value.toString().trim()))
                    .append("</cas:".concat(k).concat(">"))
                    .toString();
                LOGGER.debug("Formatted attribute for the response: [{}]", fmt);
                formattedAttributes.add(fmt);
            });
        });
        return formattedAttributes;
    }
}
