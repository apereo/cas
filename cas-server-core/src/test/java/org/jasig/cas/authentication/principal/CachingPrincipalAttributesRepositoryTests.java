/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
import java.util.ArrayList;
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

    private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    private Principal principal;

    @Before
    public void setup() {
        attributes = new HashMap<>();
        attributes.put("a1", new ArrayList(Arrays.asList(new Object[]{"v1", "v2", "v3"})));
        attributes.put("mail", new ArrayList(Arrays.asList(new Object[]{"final@example.com"})));
        attributes.put("a6", new ArrayList(Arrays.asList(new Object[]{"v16", "v26", "v63"})));
        attributes.put("a2", new ArrayList(Arrays.asList(new Object[]{"v4"})));
        attributes.put("username", new ArrayList(Arrays.asList(new Object[]{"uid"})));

        this.dao = mock(IPersonAttributeDao.class);
        final IPersonAttributes person = mock(IPersonAttributes.class);
        when(person.getName()).thenReturn("uid");
        when(person.getAttributes()).thenReturn(attributes);
        when(dao.getPerson(any(String.class))).thenReturn(person);

        this.principal = this.principalFactory.createPrincipal("uid",
                Collections.<String, Object>singletonMap("mail",
                        new ArrayList(Arrays.asList(new Object[]{"final@school.com"}))));
    }

    @Test
    public void checkExpiredCachedAttributes() throws Exception {
        assertEquals(this.principal.getAttributes().size(), 1);
        final CachingPrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(
                TimeUnit.MILLISECONDS, 100);
        repository.setAttributeRepository(this.dao);
        assertEquals(repository.getAttributes(this.principal).size(), this.attributes.size());
        assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
        Thread.sleep(200);
        this.attributes.remove("mail");
        assertTrue(repository.getAttributes(this.principal).containsKey("a2"));
        assertFalse(repository.getAttributes(this.principal).containsKey("mail"));
        repository.close();
    }

    @Test
    public void ensureCachedAttributesWithUpdate() throws Exception {
        final CachingPrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(
                TimeUnit.SECONDS, 5);
        repository.setAttributeRepository(this.dao);

        assertEquals(repository.getAttributes(this.principal).size(), this.attributes.size());
        assertTrue(repository.getAttributes(this.principal).containsKey("mail"));

        attributes.clear();
        assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
        repository.close();
    }

    @Test
    public void verifyMergingStrategyWithNoncollidingAttributeAdder() throws Exception {
        final CachingPrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(
                TimeUnit.SECONDS, 5);
        repository.setAttributeRepository(this.dao);
        repository.setMergingStrategy(CachingPrincipalAttributesRepository.MergingStrategy.ADD);

        assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
        assertEquals(repository.getAttributes(this.principal).get("mail").toString(), "final@school.com");
        ((Closeable) repository).close();
    }

    @Test
    public void verifyMergingStrategyWithReplacingAttributeAdder() throws Exception {
        final CachingPrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(
                TimeUnit.SECONDS, 5);
        repository.setAttributeRepository(this.dao);
        repository.setMergingStrategy(CachingPrincipalAttributesRepository.MergingStrategy.REPLACE);

        assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
        assertEquals(repository.getAttributes(this.principal).get("mail").toString(), "final@example.com");
        ((Closeable) repository).close();
    }

    @Test
    public void verifyMergingStrategyWithMultivaluedAttributeMerger() throws Exception {
        final CachingPrincipalAttributesRepository repository = new CachingPrincipalAttributesRepository(
                TimeUnit.SECONDS, 5);
        repository.setAttributeRepository(this.dao);
        repository.setMergingStrategy(CachingPrincipalAttributesRepository.MergingStrategy.MULTIVALUED);

        assertTrue(repository.getAttributes(this.principal).get("mail") instanceof List);

        final List<?> values = (List) repository.getAttributes(this.principal).get("mail");
        assertTrue(values.contains("final@example.com"));
        assertTrue(values.contains("final@school.com"));
        ((Closeable) repository).close();
    }
}
