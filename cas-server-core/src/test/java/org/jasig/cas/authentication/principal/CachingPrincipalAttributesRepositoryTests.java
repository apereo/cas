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

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Handles tests for {@link CachingPrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CachingPrincipalAttributesRepositoryTests {
    private Map<String, List<Object>> attributes;
    private PrincipalAttributesRepository repository;
    private IPersonAttributeDao dao;

    @After
    public void cleanUp() throws Exception {
        ((Closeable) this.repository).close();
    }

    @Before
    public void setup() {
        attributes = new HashMap<String, List<Object>>();
        attributes.put("a1", Arrays.asList(new Object[]{"v1", "v2", "v3"}));
        attributes.put("mail", Arrays.asList(new Object[]{"final@example.com"}));
        attributes.put("a6", Arrays.asList(new Object[]{"v16", "v26", "v63"}));
        attributes.put("a2", Arrays.asList(new Object[]{"v4"}));
        attributes.put("username", Arrays.asList(new Object[]{"uid"}));

        this.dao = mock(IPersonAttributeDao.class);
        final IPersonAttributes person = mock(IPersonAttributes.class);
        when(person.getName()).thenReturn("uid");
        when(person.getAttributes()).thenReturn(attributes);
        when(dao.getPerson(any(String.class))).thenReturn(person);
    }

    @Test
    public void testCachedAttributes() {
        this.repository = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("mail", "final@example.com"));
        assertEquals(this.repository.getAttributes("uid").size(), 1);
        assertTrue(this.repository.getAttributes("uid").containsKey("mail"));
    }

    @Test
    public void testExpiredCachedAttributes() throws Exception {
        this.repository = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("mail", "final@example.com"),
                TimeUnit.MILLISECONDS, 100);
        assertEquals(this.repository.getAttributes("uid").size(), 1);
        assertTrue(this.repository.getAttributes("uid").containsKey("mail"));
        Thread.sleep(200);
        assertTrue(this.repository.getAttributes("uid").containsKey("a2"));
    }

    @Test
    public void testCachedAttributesWithUpdate() throws Exception {
        this.repository = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("mail", "final@example.com"),
                TimeUnit.SECONDS, 5);
        assertEquals(this.repository.getAttributes("uid").size(), 1);
        assertTrue(this.repository.getAttributes("uid").containsKey("mail"));

        attributes.clear();
        assertTrue(this.repository.getAttributes("uid").containsKey("mail"));
    }
}
