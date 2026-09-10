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
                val name = toXmlElementName(CasProtocolAttributesRenderer.sanitizeAttributeName(attributeName));
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

    /**
     * Rendered attribute lines are placed into the validation response as markup rather than as
     * text, which makes the attribute name part of the document structure. Every character that is
     * not allowed in an XML name is replaced, so a name carrying markup or quoting characters cannot
     * escape the element it is meant to define. Names are also required to start with a character
     * that may begin an XML name.
     *
     * @param attributeName the sanitized attribute name
     * @return a name that is safe to render as an XML element name
     */
    protected static String toXmlElementName(final String attributeName) {
        val elementName = new StringBuilder(attributeName.length());
        attributeName.codePoints().forEach(codePoint -> elementName.appendCodePoint(isNameChar(codePoint) ? codePoint : '_'));
        if (elementName.isEmpty() || !isNameStartChar(elementName.codePointAt(0))) {
            elementName.insert(0, '_');
        }
        return elementName.toString();
    }

    private static boolean isNameStartChar(final int codePoint) {
        return codePoint == '_' || Character.isLetter(codePoint);
    }

    private static boolean isNameChar(final int codePoint) {
        return isNameStartChar(codePoint) || Character.isDigit(codePoint) || codePoint == '-' || codePoint == '.';
    }
}
