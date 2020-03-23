package org.apereo.cas.support.wsfederation;

import org.apereo.cas.support.wsfederation.attributes.WsFederationAttributeMutator;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link WsFederationAttributeMutator}.
 *
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationAttributeMutatorTests extends AbstractWsFederationTests {

    private static final String UPN_PARAM = "upn";

    @Test
    public void verifyModifyAttributes() {
        val attributes = new HashMap<String, List<Object>>();

        val values = new ArrayList<Object>();
        values.add("test@example.com");
        attributes.put(UPN_PARAM, values);

        val instance = new WsFederationAttributeMutatorImpl();
        instance.modifyAttributes(attributes);

        assertTrue(attributes.containsKey("test"));
        assertTrue("newtest".equalsIgnoreCase(attributes.get("test").get(0).toString()));
        assertTrue(attributes.containsKey(UPN_PARAM));
        assertTrue("testing".equalsIgnoreCase(attributes.get(UPN_PARAM).get(0).toString()));
    }

    private static class WsFederationAttributeMutatorImpl implements WsFederationAttributeMutator {
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
