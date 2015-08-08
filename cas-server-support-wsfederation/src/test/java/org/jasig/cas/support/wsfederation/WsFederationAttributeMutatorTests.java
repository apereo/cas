/*
 * Copyright 2014 Unicon, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jasig.cas.support.wsfederation;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test cases for {@link WsFederationAttributeMutator}.
 * @author John Gasper
 * @since 4.2.0
 */
public class WsFederationAttributeMutatorTests extends AbstractWsFederationTests {

    @Test
    public void testModifyAttributes() {
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("upn", "test@example.com");
        
        final WsFederationAttributeMutator instance = new WsFederationAttributeMutatorImpl();
        instance.modifyAttributes(attributes);
        assertTrue("testModifyAttributes() - true", attributes.containsKey("test"));
        assertTrue("testModifyAttributes() - true",
                attributes.get("test").toString().equalsIgnoreCase("test"));
        assertTrue("testModifyAttributes() - true",
                attributes.containsKey("upn"));
        assertTrue("testModifyAttributes() - true",
                attributes.get("upn").toString().equalsIgnoreCase("testing"));
    }

    private static class WsFederationAttributeMutatorImpl implements WsFederationAttributeMutator {
        private static final long serialVersionUID = -1858140387002752668L;

        /**
         * @param attributes a map of attributes
         */
        public void modifyAttributes(final Map<String, Object> attributes) {
            attributes.put("test", "test");
            attributes.put("upn", "testing");
        }
    }
}
