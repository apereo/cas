package org.apereo.cas.web.view.attributes;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCas30ProtocolAttributesRendererTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Attributes")
class DefaultCas30ProtocolAttributesRendererTests {
    private static final Pattern SAFE_ELEMENT = Pattern.compile(
        "^<cas:([A-Za-z_][A-Za-z0-9._-]*)>[^<>]*</cas:\\1>$");

    @Test
    void verifyAction() {
        val r = new DefaultCas30ProtocolAttributesRenderer();
        val results = CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap();
        assertFalse(r.render((Map) results).isEmpty());
    }

    @Test
    void verifyAttributeNameCannotCarryMarkupIntoTheResponse() {
        val renderer = new DefaultCas30ProtocolAttributesRenderer();
        val hostileNames = List.of(
            "given><script>alert(1)</script",
            "given name=\"x\"",
            "given/><cas:admin",
            "given:name",
            "given\nname");
        hostileNames.forEach(name -> {
            val rendered = renderer.render(Map.<String, Object>of(name, "casuser"));
            val line = rendered.iterator().next();
            assertTrue(SAFE_ELEMENT.matcher(line).matches(),
                "Name [%s] rendered as [%s], which is not a single well-formed element".formatted(name, line));
        });
    }

    @Test
    void verifyAttributeNameIsGivenAValidStartCharacter() {
        val renderer = new DefaultCas30ProtocolAttributesRenderer();
        val line = renderer.render(Map.<String, Object>of("1st-choice", "casuser")).iterator().next();
        assertTrue(SAFE_ELEMENT.matcher(line).matches(), line);
        assertTrue(line.startsWith("<cas:_1st-choice>"), line);
    }

    @Test
    void verifyOrdinaryAttributeNamesAreLeftAlone() {
        val renderer = new DefaultCas30ProtocolAttributesRenderer();
        assertEquals("<cas:eduPersonAffiliation>staff</cas:eduPersonAffiliation>",
            renderer.render(Map.<String, Object>of("eduPersonAffiliation", "staff")).iterator().next());
        assertEquals("<cas:given_name>casuser</cas:given_name>",
            renderer.render(Map.<String, Object>of("given name", "casuser")).iterator().next());
    }
}
