package org.apereo.cas.support.wsfederation;

import module java.base;
import org.apereo.cas.support.wsfederation.attributes.WsFederationAttributeMutator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link WsFederationAttributeMutator}.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Tag("WSFederation")
class WsFederationAttributeMutatorTests extends AbstractWsFederationTests {

    private static final String UPN_PARAM = "upn";

    @Test
    void verifyModifyAttributes() {
        val attributes = new HashMap<String, List<Object>>();

        val values = new ArrayList<>();
        values.add("test@example.com");
        attributes.put(UPN_PARAM, values);

        val instance = new TestWsFederationAttributeMutator();
        instance.modifyAttributes(attributes);

        assertTrue(attributes.containsKey("test"));
        assertTrue("newtest".equalsIgnoreCase(attributes.get("test").getFirst().toString()));
        assertTrue(attributes.containsKey(UPN_PARAM));
        assertTrue("testing".equalsIgnoreCase(attributes.get(UPN_PARAM).getFirst().toString()));
    }

    private static final class TestWsFederationAttributeMutator implements WsFederationAttributeMutator {
        @Serial
        private static final long serialVersionUID = -1858140387002752668L;

        @Override
        public Map<String, List<Object>> modifyAttributes(final Map<String, List<Object>> attributes) {
            List<Object> values = new ArrayList<>();
            values.add("newtest");
            attributes.put("test", values);

            values = new ArrayList<>();
            values.add("testing");
            attributes.put(UPN_PARAM, values);
            return attributes;
        }
    }
}
