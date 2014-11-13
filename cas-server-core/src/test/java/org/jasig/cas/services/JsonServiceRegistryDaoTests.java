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
package org.jasig.cas.services;

import org.apache.commons.io.FileUtils;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Handles test cases for {@link JsonServiceRegistryDao}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class JsonServiceRegistryDaoTests {
    private ServiceRegistryDao dao;
    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    public JsonServiceRegistryDaoTests() throws Exception {
        this.dao = new JsonServiceRegistryDao(RESOURCE.getFile());
    }

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void testSaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithNonExistentServiceAndNoAttributes");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void testSaveAttributeReleasePolicy() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicy");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void testSaveMethodWithExistingServiceNoAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithExistingServiceNoAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        this.dao.save(r);
        r.setTheme("mytheme");

        this.dao.save(r);

        final RegisteredService r3 = this.dao.findServiceById(r.getId());
        assertEquals(r, r3);
    }

    @Test
    public void testSaveAttributeReleasePolicyMappingRules() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicyMappingRules");
        r.setServiceId("testId");

        final Map<String, String> map = new HashMap<String, String>();
        map.put("attr1", "newattr1");
        map.put("attr2", "newattr2");
        map.put("attr2", "newattr3");


        final ReturnMappedAttributeReleasePolicy policy = new ReturnMappedAttributeReleasePolicy();
        policy.setAllowedAttributes(map);
        r.setAttributeReleasePolicy(policy);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void testSaveAttributeReleasePolicyAllowedAttrRules() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRules");
        r.setServiceId("testId");

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter");
        r.setServiceId("testId");
        r.setEnabled(true);
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);
        r.setSsoEnabled(false);
        r.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https://.+"));
        r.setRequiredHandlers(new HashSet<String>(Arrays.asList("h1", "h2")));

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);
        r.getAttributeReleasePolicy().setAttributeFilter(new RegisteredServiceRegexAttributeFilter("\\w+"));

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void testServiceType() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("testServiceType");
        r.setEnabled(true);
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);

        final RegisteredService r2 = this.dao.save(r);
        assertTrue(r2 instanceof  RegexRegisteredService);
    }

    @Test(expected=RuntimeException.class)
    public void testServiceWithInvalidFileName() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("hell/o@world:*");
        r.setEnabled(true);
        r.setEvaluationOrder(1000);

        final RegisteredService r2 = this.dao.save(r);
    }

    @Test
    public void testServiceRemovals() {
        final List<RegisteredService> list = new ArrayList<>(5);
        for (int i = 1; i < 5; i++) {
            final RegexRegisteredService r = new RegexRegisteredService();
            r.setServiceId("^https://.+");
            r.setName("testServiceType");
            r.setEnabled(true);
            r.setTheme("testtheme");
            r.setEvaluationOrder(1000);
            r.setId(i * 100);
            list.add(this.dao.save(r));
        }

        for (final RegisteredService r2 : list) {
            this.dao.delete(r2);
            assertNull(this.dao.findServiceById(r2.getId()));
        }

    }

}
