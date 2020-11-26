package org.apereo.cas.web.view.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AttributeValuesPerLineProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Attributes")
public class AttributeValuesPerLineProtocolAttributesRendererTests {
    @Test
    public void verifyAction() {
        val r = new AttributeValuesPerLineProtocolAttributesRenderer();
        val results = CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render((Map) results).isEmpty());
    }

    @Test
    public void verifyActionWithSpaces() {
        val r = new AttributeValuesPerLineProtocolAttributesRenderer();
        val results = Map.of("attribute name", "attribute-value");
        val rendered = r.render((Map) results);
        assertFalse(rendered.isEmpty());
    }
}
