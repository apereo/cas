package org.apereo.cas.web.view.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link AttributeValuesPerLineProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class AttributeValuesPerLineProtocolAttributesRendererTests {
    @Test
    public void verifyAction() {
        val r = new AttributeValuesPerLineProtocolAttributesRenderer();
        val results = CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render((Map) results).isEmpty());
    }
}
