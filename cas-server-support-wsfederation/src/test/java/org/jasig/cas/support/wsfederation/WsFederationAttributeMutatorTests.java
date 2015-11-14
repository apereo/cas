package org.jasig.cas.support.wsfederation;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test cases for {@link WsFederationAttributeMutator}.
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationAttributeMutatorTests extends AbstractWsFederationTests {

    @Test
    public void verifyModifyAttributes() {
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("upn", "test@example.com");
        
        final WsFederationAttributeMutator instance = new WsFederationAttributeMutatorImpl();
        instance.modifyAttributes(attributes);
        assertTrue("testModifyAttributes() - true", attributes.containsKey("test"));
        assertTrue("testModifyAttributes() - true",
                "newtest".equalsIgnoreCase(attributes.get("test").toString()));

        assertTrue("testModifyAttributes() - true",
                attributes.containsKey("upn"));
        assertTrue("testModifyAttributes() - true",
                "testing".equalsIgnoreCase(attributes.get("upn").toString()));
    }

    private static class WsFederationAttributeMutatorImpl implements WsFederationAttributeMutator {
        private static final long serialVersionUID = -1858140387002752668L;

        @Override
        public void modifyAttributes(final Map<String, Object> attributes) {
            attributes.put("test", "newtest");
            attributes.put("upn", "testing");
        }
    }
}
