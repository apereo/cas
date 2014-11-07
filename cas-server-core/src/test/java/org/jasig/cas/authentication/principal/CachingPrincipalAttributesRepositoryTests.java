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

    private IPersonAttributeDao dao;

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
    public void testCachedAttributes() throws Exception {
        final PrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("mail", "final@example.com"));
        assertEquals(repository.getAttributes("uid").size(), 1);
        assertTrue(repository.getAttributes("uid").containsKey("mail"));
        ((Closeable) repository).close();
    }

    @Test
    public void testExpiredCachedAttributes() throws Exception {
        final PrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("mail", "final@example.com"),
                TimeUnit.MILLISECONDS, 100);
        assertEquals(repository.getAttributes("uid").size(), 1);
        assertTrue(repository.getAttributes("uid").containsKey("mail"));
        Thread.sleep(200);
        assertTrue(repository.getAttributes("uid").containsKey("a2"));
        ((Closeable) repository).close();
    }

    @Test
    public void testCachedAttributesWithUpdate() throws Exception {
        final PrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("mail", "final@example.com"),
                TimeUnit.SECONDS, 5);
        assertEquals(repository.getAttributes("uid").size(), 1);
        assertTrue(repository.getAttributes("uid").containsKey("mail"));

        attributes.clear();
        assertTrue(repository.getAttributes("uid").containsKey("mail"));
        ((Closeable) repository).close();
    }

    @Test
    public void testPrincipalFactoryWithSameCachingAttributess() throws Exception {
        final PrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("address", "final@example.com"),
                TimeUnit.MILLISECONDS, 200);

        final DefaultPrincipalFactory factory1 = new DefaultPrincipalFactory(repository);
        final DefaultPrincipalFactory factory2 = new DefaultPrincipalFactory(repository);

        final Principal p1 = factory1.createPrincipal("uid1");
        final Principal p2 = factory2.createPrincipal("uid2");

        assertFalse(p1.getAttributes().containsKey("mail"));
        assertFalse(p2.getAttributes().containsKey("mail"));
        assertTrue(p1.getAttributes().containsKey("address"));
        assertTrue(p2.getAttributes().containsKey("address"));

        Thread.sleep(400);

        assertTrue(p1.getAttributes().containsKey("mail"));
        assertTrue(p2.getAttributes().containsKey("mail"));
        assertFalse(p1.getAttributes().containsKey("address"));
        assertFalse(p2.getAttributes().containsKey("address"));
        ((Closeable) repository).close();
    }

    @Test
    public void testPrincipalFactoryWithDifferentCachingAttributesForManyPrincipals() throws Exception {
        final PrincipalAttributesRepository repository1 = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("address", "final@example.com"),
                TimeUnit.MILLISECONDS, 200);

        final PrincipalAttributesRepository repository2 = new CachingPrincipalAttributesRepository(this.dao,
                Collections.<String, Object>singletonMap("address", "final@example.com"),
                TimeUnit.MILLISECONDS, 700);

        final DefaultPrincipalFactory factory1 = new DefaultPrincipalFactory(repository1);
        final DefaultPrincipalFactory factory2 = new DefaultPrincipalFactory(repository2);

        final Principal p1 = factory1.createPrincipal("uid1");
        final Principal p2 = factory2.createPrincipal("uid2");

        assertFalse(p1.getAttributes().containsKey("mail"));
        assertFalse(p2.getAttributes().containsKey("mail"));
        assertTrue(p1.getAttributes().containsKey("address"));
        assertTrue(p2.getAttributes().containsKey("address"));

        Thread.sleep(400);

        assertTrue(p1.getAttributes().containsKey("mail"));
        assertFalse(p1.getAttributes().containsKey("address"));

        assertTrue(p2.getAttributes().containsKey("address"));
        assertFalse(p2.getAttributes().containsKey("mail"));

        ((Closeable) repository2).close();
        ((Closeable) repository1).close();
    }

}
