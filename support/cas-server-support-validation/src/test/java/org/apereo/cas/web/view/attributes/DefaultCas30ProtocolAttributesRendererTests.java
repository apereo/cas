package org.apereo.cas.web.view.attributes;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultCas30ProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultCas30ProtocolAttributesRendererTests {
    @Test
    public void verifyAction() {
        final var r = new DefaultCas30ProtocolAttributesRenderer();
        final Map results = CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render(results).isEmpty());
    }
}
