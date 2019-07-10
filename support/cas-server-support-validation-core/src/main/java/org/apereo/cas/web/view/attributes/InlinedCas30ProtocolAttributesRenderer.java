package org.apereo.cas.web.view.attributes;

import lombok.val;

/**
 * This is {@link InlinedCas30ProtocolAttributesRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class InlinedCas30ProtocolAttributesRenderer extends DefaultCas30ProtocolAttributesRenderer {

    @Override
    protected String buildSingleAttributeDefinitionLine(final String attributeName, final Object value) {
        val encodedValue = encodeAttributeValue(value);
        return new StringBuilder()
            .append("<cas:attribute name=\"".concat(attributeName).concat("\" value=\"".concat(encodedValue)).concat("\"></cas:attribute>"))
            .toString();
    }
}
