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

package org.jasig.cas.authentication.principal;

import org.jasig.cas.util.PrincipalUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 */
public class NotCachingAttributesPrincipalFactoryTests {
    @Test
    public void testPrincipalCreation() {

        final Map<String, List<Object>> map = new HashMap<String, List<Object>>();
        map.put("a1", Arrays.asList(new Object[]{"v1", "v2", "v3"}));
        map.put("a6", Arrays.asList(new Object[]{"v16", "v26", "v63"}));
        map.put("a2", Arrays.asList(new Object[] {"v4"}));
        map.put("username", Arrays.asList(new Object[] {"user"}));

        final IPersonAttributeDao dao = mock(IPersonAttributeDao.class);
        final IPersonAttributes person = mock(IPersonAttributes.class);
        when(person.getName()).thenReturn("user");
        when(person.getAttributes()).thenReturn(map);

        when(dao.getPerson(any(String.class))).thenReturn(person);
        final NotCachingAttributesPrincipalFactory fact = new NotCachingAttributesPrincipalFactory(dao);

        final Principal p = fact.createPrincipal("user",
                PrincipalUtils.convertPersonAttributesToPrincipalAttributes("user", dao));

        assertTrue(p instanceof NotCachingAttributesPrincipal);
        assertEquals(p.getAttributes().size(), map.size());
        assertTrue(p.getAttributes().containsKey("a2"));
        assertEquals(p.getAttributes().get("a2"), "v4");

        map.put("a6", Arrays.asList(new Object[]{"v26", "v6311"}));
        map.remove("a2");
        assertEquals(p.getAttributes().size(), map.size());
        assertFalse(p.getAttributes().containsKey("a2"));
    }
}
