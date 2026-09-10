package org.apereo.cas.web.view.attributes;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InlinedCas30ProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Attributes")
class InlinedCas30ProtocolAttributesRendererTests {
    @Test
    void verifyAction() {
        val r = new InlinedCas30ProtocolAttributesRenderer();
        val results = (Map) CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render(results).isEmpty());
    }

    @Test
    void verifyAttributeNameCannotBreakOutOfTheNameAttribute() {
        val renderer = new InlinedCas30ProtocolAttributesRenderer();
        val line = renderer.render(Map.<String, Object>of("given\" value=\"pwned\" x=\"", "casuser")).iterator().next();
        assertEquals("<cas:attribute name=\"given__value__pwned__x__\" value=\"casuser\"></cas:attribute>", line);
    }
}
