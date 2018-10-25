package org.apereo.cas.web.view.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCas30ProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultCas30ProtocolAttributesRendererTests {
    @Test
    public void verifyAction() {
        val r = new DefaultCas30ProtocolAttributesRenderer();
        val results = CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render((Map) results).isEmpty());
    }
}
