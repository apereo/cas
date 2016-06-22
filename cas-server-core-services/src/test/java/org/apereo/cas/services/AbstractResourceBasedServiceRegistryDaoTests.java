package org.apereo.cas.services;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractResourceBasedServiceRegistryDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractResourceBasedServiceRegistryDaoTests {
    public static final ClassPathResource RESOURCE = new ClassPathResource("services");

    protected ServiceRegistryDao dao;

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkLoadingOfServiceFiles() throws Exception {
        prepTests();
        verifySaveAttributeReleasePolicyMappingRules();
        verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter();
        assertEquals(this.dao.load().size(), 2);
    }
    
    @Test
    public void checkSaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegexRegisteredService r = new RegexRegisteredService();
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
    public void execSaveWithAuthnMethodPolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("execSaveWithAuthnMethodPolicy");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final DefaultRegisteredServiceMultifactorPolicy policy =
                new DefaultRegisteredServiceMultifactorPolicy();
        policy.setFailureMode(RegisteredServiceMultifactorPolicy.FailureModes.PHANTOM);

        final Set<String> set = new HashSet<>();
        set.add("duoAuthenticationProvider");
        policy.setMultifactorAuthenticationProviders(set);
        policy.setPrincipalAttributeNameTrigger("memberOf");
        policy.setPrincipalAttributeValueToMatch("cas|CAS|admin");
        r.setMultifactorPolicy(policy);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }

    @Test
    public void execSaveMethodWithDefaultUsernameAttribute() {
        final RegexRegisteredService r = new RegexRegisteredService();
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
        final RegexRegisteredService r = new RegexRegisteredService();
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
        final RegexRegisteredService r = new RegexRegisteredService();
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
        final RegexRegisteredService r = new RegexRegisteredService();
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
        final RegexRegisteredService r = new RegexRegisteredService();
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
        final RegexRegisteredService r = new RegexRegisteredService();
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
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("testSaveAttributeReleasePolicyAllowedAttrRules");
        r.setServiceId("testId");

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Lists.newArrayList("1", "2", "3"));
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
        r.setRequiredHandlers(new HashSet<>(Lists.newArrayList("h1", "h2")));

        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Lists.newArrayList("1", "2", "3"));
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
        assertTrue(r2 instanceof RegexRegisteredService);
    }

    @Test(expected=RuntimeException.class)
    public void verifyServiceWithInvalidFileName() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("hell/o@world:*");
        r.setEvaluationOrder(1000);

        this.dao.save(r);
    }
    
    @Test
    public void verifyServiceRemovals() throws Exception{
        final List<RegisteredService> list = new ArrayList<>(5);
        IntStream.range(1, 5).forEach(i -> {
            final RegexRegisteredService r = new RegexRegisteredService();
            r.setServiceId("^https://.+");
            r.setName("testServiceType");
            r.setTheme("testtheme");
            r.setEvaluationOrder(1000);
            r.setId(i * 100);
            list.add(this.dao.save(r));
        });

        list.stream().forEach(r2 -> {
            try {
                Thread.sleep(500);
                this.dao.delete(r2);
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                throw Throwables.propagate(e);
            }
            assertNull(this.dao.findServiceById(r2.getId()));
        });
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
        attrs.put("memberOf", Sets.newHashSet(Lists.newArrayList("v4, v5, v6")));
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
        r.setName("verifyAAccessStrategyWithStarEndDate");
        r.setId(62);

        final TimeBasedRegisteredServiceAccessStrategy authz =
                new TimeBasedRegisteredServiceAccessStrategy(true, false);

        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(10).toString());

        authz.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        r.setAccessStrategy(authz);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyAccessStrategyWithEndpoint() throws Exception {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("^https://.+");
        r.setName("verifyAccessStrategyWithEndpoint");
        r.setId(62);

        final RemoteEndpointServiceAccessStrategy authz = new RemoteEndpointServiceAccessStrategy();

        authz.setEndpointUrl("http://www.google.com?this=that");
        authz.setAcceptableResponseCodes("200,405,403");
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
