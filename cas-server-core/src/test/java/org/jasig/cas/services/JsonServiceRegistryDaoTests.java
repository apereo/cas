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

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.jasig.cas.authentication.principal.CachingPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Handles test cases for {@link JsonServiceRegistryDao}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class JsonServiceRegistryDaoTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private final ServiceRegistryDao dao;

    public JsonServiceRegistryDaoTests() throws Exception {
        this.dao = new JsonServiceRegistryDao(RESOURCE.getFile());
    }

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkSaveMethodWithNonExistentServiceAndNoAttributes() {
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
    public void execSaveMethodWithDefaultUsernameAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithDefaultUsernameAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }

    @Test
    public void ensureSaveMethodWithDefaultPrincipalAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithDefaultPrincipalAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn"));
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }
    @Test
    public void verifySaveMethodWithDefaultAnonymousAttribute() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveMethodWithDefaultAnonymousAttribute");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider(
                new ShibbolethCompatiblePersistentIdGenerator("helloworld")
        ));
        final RegisteredService r2 = this.dao.save(r);
        this.dao.load();
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        final AnonymousRegisteredServiceUsernameAttributeProvider anon =
                (AnonymousRegisteredServiceUsernameAttributeProvider) r3.getUsernameAttributeProvider();
        final ShibbolethCompatiblePersistentIdGenerator ss =
                (ShibbolethCompatiblePersistentIdGenerator) anon.getPersistentIdGenerator();
        assertEquals(new String(ss.getSalt()), "helloworld");
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveAttributeReleasePolicy() {
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
    public void verifySaveMethodWithExistingServiceNoAttribute() {
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
    public void verifySaveAttributeReleasePolicyMappingRules() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicyMappingRules");
        r.setServiceId("testId");

        final Map<String, String> map = new HashMap<>();
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
    public void verifySaveAttributeReleasePolicyAllowedAttrRules() {
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
    public void verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter");
        r.setServiceId("testId");
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);
        r.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
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
    public void verifyServiceType() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("testServiceType");
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);

        final RegisteredService r2 = this.dao.save(r);
        assertTrue(r2 instanceof  RegexRegisteredService);
    }

    @Test(expected=RuntimeException.class)
    public void verifyServiceWithInvalidFileName() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("hell/o@world:*");
        r.setEvaluationOrder(1000);

        final RegisteredService r2 = this.dao.save(r);
    }

    @Test
    public void checkLoadingOfJsonServiceFiles() throws Exception {
        prepTests();
        verifySaveAttributeReleasePolicyAllowedAttrRulesWithCaching();
        verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter();
        assertEquals(this.dao.load().size(), 2);
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRulesWithCaching() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRulesWithCaching");
        r.setServiceId("testId");

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));

        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put("values", Arrays.asList(new Object[]{"v1", "v2", "v3"}));

        final CachingPrincipalAttributesRepository repository =
                new CachingPrincipalAttributesRepository(
                        TimeUnit.MILLISECONDS, 100);
        repository.setMergingStrategy(CachingPrincipalAttributesRepository.MergingStrategy.REPLACE);

        policy.setPrincipalAttributesRepository(repository);
        r.setAttributeReleasePolicy(policy);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());


        this.dao.load();

    }

    @Test
    public void verifyServiceRemovals() throws Exception{
        final List<RegisteredService> list = new ArrayList<>(5);
        for (int i = 1; i < 5; i++) {
            final RegexRegisteredService r = new RegexRegisteredService();
            r.setServiceId("^https://.+");
            r.setName("testServiceType");
            r.setTheme("testtheme");
            r.setEvaluationOrder(1000);
            r.setId(i * 100);
            list.add(this.dao.save(r));
        }

        for (final RegisteredService r2 : list) {
            this.dao.delete(r2);
            Thread.sleep(3000);
            assertNull(this.dao.findServiceById(r2.getId()));
        }

    }

    @Test
    public void checkForAuthorizationStrategy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("checkForAuthorizationStrategy");
        r.setId(42);

        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(false, false);

        final Map<String, Set<String>> attrs = new HashMap<>();
        attrs.put("cn", Sets.newHashSet("v1, v2, v3"));
        attrs.put("memberOf", Sets.newHashSet(Arrays.asList("v4, v5, v6")));
        authz.setRequiredAttributes(attrs);
        r.setAccessStrategy(authz);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyAccessStrategyWithStarEndDate() throws Exception {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("verifyAccessStrategyWithStarEndDate");
        r.setId(62);

        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(true, false);

        authz.setStartingDateTime(DateTime.now().plusDays(1).toString());
        authz.setEndingDateTime(DateTime.now().plusDays(10).toString());

        authz.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        r.setAccessStrategy(authz);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void serializePublicKeyForServiceAndVerify() throws Exception {
        final RegisteredServicePublicKey publicKey = new RegisteredServicePublicKeyImpl(
                "classpath:RSA1024Public.key", "RSA");

        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("serializePublicKeyForServiceAndVerify");
        r.setId(4245);
        r.setPublicKey(publicKey);

        this.dao.save(r);
        final List<RegisteredService> list = this.dao.load();
        assertNotNull(this.dao.findServiceById(r.getId()));
    }

    @Test
    public void verifyEdit()  {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        this.dao.save(r);

        final List<RegisteredService> services = this.dao.load();

        final RegisteredService r2 = services.get(0);

        r.setId(r2.getId());
        r.setTheme("mytheme");

        this.dao.save(r);

        final RegisteredService r3 = this.dao.findServiceById(r.getId());

        assertEquals(r, r3);
        assertEquals(r.getTheme(), r3.getTheme());
        this.dao.delete(r);
    }

    @Test
    public void persistCustomServiceProperties() throws Exception {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("persistCustomServiceProperties");
        r.setId(4245);

        final Map<String, RegisteredServiceProperty> properties = new HashMap<>();
        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        properties.put("field1", property);


        final DefaultRegisteredServiceProperty property2 = new DefaultRegisteredServiceProperty();
        final Set<String> values2 = new HashSet<>();
        values2.add("value12");
        values2.add("value22");
        property2.setValues(values2);
        properties.put("field2", property2);

        r.setProperties(properties);

        this.dao.save(r);
        final List<RegisteredService> list = this.dao.load();
        assertNotNull(this.dao.findServiceById(r.getId()));
        assertEquals(r.getProperties().size(), 2);
        assertNotNull(r.getProperties().get("field1"));

        final RegisteredServiceProperty prop = r.getProperties().get("field1");
        assertEquals(prop.getValues().size(), 2);
    }
}
