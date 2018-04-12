package org.apereo.cas.web.view.attributes;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link InlinedCas30ProtocolAttributesRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class InlinedCas30ProtocolAttributesRenderer extends DefaultCas30ProtocolAttributesRenderer {

    @Override
    protected String buildSingleAttributeDefinitionLine(final String attributeName, final Object value) {
        final var encodedValue = encodeAttributeValue(value);
        return new StringBuilder()
            .append("<cas:attribute name=\"".concat(attributeName).concat("\" value=\"".concat(encodedValue)).concat("\"></cas:attribute>"))
            .toString();
    }
}
