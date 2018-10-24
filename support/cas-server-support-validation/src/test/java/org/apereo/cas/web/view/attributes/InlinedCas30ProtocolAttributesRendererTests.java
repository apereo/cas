package org.apereo.cas.web.view.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link InlinedCas30ProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class InlinedCas30ProtocolAttributesRendererTests {
    @Test
    public void verifyAction() {
        val r = new InlinedCas30ProtocolAttributesRenderer();
        val results = (Map) CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render(results).isEmpty());
    }
}
