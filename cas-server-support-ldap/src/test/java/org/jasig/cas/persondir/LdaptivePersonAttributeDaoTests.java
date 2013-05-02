/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.persondir;

import java.util.List;
import java.util.Map;

import org.jasig.cas.RequiredConfigurationProfileValueSource;
import org.jasig.cas.authentication.principal.TestPrincipal;
import org.jasig.services.persondir.IPersonAttributes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link LdaptivePersonAttributeDao}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext-test.xml"})
@ProfileValueSourceConfiguration(RequiredConfigurationProfileValueSource.class)
@IfProfileValue(name = "resolverConfig", value = "true")
public class LdaptivePersonAttributeDaoTests {

    @Autowired
    private LdaptivePersonAttributeDao attributeDao;

    @Autowired
    @Qualifier("testPrincipals")
    private Resource testPrincipals;

    @Test
    public void testGetPerson() throws Exception {
        IPersonAttributes actual;
        for (TestPrincipal expected : TestPrincipal.loadFromResource(testPrincipals)) {
            actual = attributeDao.getPerson(expected.getUserName());
            assertNotNull(actual);
            assertEquals(expected.getId(), actual.getName());
            assertAttributesSame(expected.getAttributes(), actual.getAttributes());
        }
    }

    /**
     * Determines whether the given attibute maps are equal irrespective of value ordering.
     *
     * @param a First set of attributes.
     * @param b Second set of attributes.
     */
    private static void assertAttributesSame(final Map<String, Object> a, final Map<String, List<Object>> b) {
        assertEquals(a.size(), b.size());
        Object valueA;
        List<Object> valueB;
        for (String key : a.keySet()) {
            valueA = a.get(key);
            valueB = b.get(key);
            if (valueA instanceof List) {
                for (Object item : ((List) valueA)) {
                    assertTrue(valueB.contains(item));
                }
            } else {
                assertEquals(1, valueB.size());
                assertEquals(valueA, valueB.get(0));
            }
        }
    }
}
