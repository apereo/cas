package org.apereo.cas.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractResourceBasedServiceRegistryDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractResourceBasedServiceRegistryDaoTests {

    public static final ClassPathResource RESOURCE = new ClassPathResource("services");
    private static final String SERVICE_ID = "testId";
    private static final String THEME = "theme";
    private static final String DESCRIPTION = "description";
    private static final String HTTPS_SERVICE_ID = "^https://.+";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveWithAuthnMethodPolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("execSaveWithAuthnMethodPolicy");
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);

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
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);
        r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }

    @Test
    public void execSaveMethodWithConsentPolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("execSaveMethodWithConsentPolicy");
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);
        final ReturnAllAttributeReleasePolicy policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("test"),
                CollectionUtils.wrapSet("test")));
        r.setAttributeReleasePolicy(policy);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }

    @Test
    public void ensureSaveMethodWithDefaultPrincipalAttribute() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("testSaveMethodWithDefaultPrincipalAttribute");
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);
        r.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn", "UPPER"));
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r2, r);
    }

    @Test
    public void verifySaveMethodWithDefaultAnonymousAttribute() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("testSaveMethodWithDefaultAnonymousAttribute");
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);
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
        assertEquals(ss.getSalt(), "helloworld");
        assertEquals(r2, r3);
    }

    @Test
    public void verifyServiceExpirationPolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("verifyServiceExpirationPolicy");
        r.setServiceId(SERVICE_ID);
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDate.now()));

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getExpirationPolicy());
        assertEquals(r2.getExpirationPolicy(), r3.getExpirationPolicy());
    }
    
    @Test
    public void verifySaveAttributeReleasePolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("testSaveAttributeReleasePolicy");
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);
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
        r.setServiceId(SERVICE_ID);
        r.setTheme(THEME);
        r.setDescription(DESCRIPTION);
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
        r.setServiceId(SERVICE_ID);

        final Multimap<String, String> map = ArrayListMultimap.create();
        map.put("attr1", "newattr1");
        map.put("attr2", "newattr2");
        map.put("attr2", "newattr3");

        final ReturnMappedAttributeReleasePolicy policy = new ReturnMappedAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap(map));
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
        r.setServiceId(SERVICE_ID);

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
        r.setServiceId(SERVICE_ID);
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);
        r.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        r.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https://.+"));
        r.setRequiredHandlers(Stream.of("h1", "h2").collect(Collectors.toSet()));

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
        r.setServiceId(HTTPS_SERVICE_ID);
        r.setName("testServiceType");
        r.setTheme("testtheme");
        r.setEvaluationOrder(1000);

        final RegisteredService r2 = this.dao.save(r);
        assertTrue(r2 instanceof RegexRegisteredService);
    }

    @Test
    public void verifyServiceWithInvalidFileName() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId(HTTPS_SERVICE_ID);
        r.setName("hell/o@world:*");
        r.setEvaluationOrder(1000);
        this.thrown.expect(IllegalArgumentException.class);
        this.dao.save(r);
    }

    @Test
    public void verifyServiceRemovals() {
        final List<RegisteredService> list = new ArrayList<>(5);
        IntStream.range(1, 5).forEach(i -> {
            final RegexRegisteredService r = new RegexRegisteredService();
            r.setServiceId(HTTPS_SERVICE_ID);
            r.setName("testServiceType");
            r.setTheme("testtheme");
            r.setEvaluationOrder(1000);
            r.setId(i * 100);
            list.add(this.dao.save(r));
        });

        list.forEach(r2 -> {
            try {
                Thread.sleep(500);
                this.dao.delete(r2);
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            assertNull(this.dao.findServiceById(r2.getId()));
        });
    }

    @Test
    public void checkForAuthorizationStrategy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId(HTTPS_SERVICE_ID);
        r.setName("checkForAuthorizationStrategy");
        r.setId(42);

        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(false, false);

        final Map<String, Set<String>> attrs = new HashMap<>();
        attrs.put("cn", Collections.singleton("v1, v2, v3"));
        attrs.put("memberOf", Collections.singleton("v4, v5, v6"));
        authz.setRequiredAttributes(attrs);
        r.setAccessStrategy(authz);

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyAccessStrategyWithStarEndDate() throws Exception {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId(HTTPS_SERVICE_ID);
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
        r.setServiceId(HTTPS_SERVICE_ID);
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
    public void serializePublicKeyForServiceAndVerify() {
        final RegisteredServicePublicKey publicKey = new RegisteredServicePublicKeyImpl(
                "classpath:RSA1024Public.key", "RSA");

        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId(HTTPS_SERVICE_ID);
        r.setName("serializePublicKeyForServiceAndVerify");
        r.setId(4245);
        r.setPublicKey(publicKey);

        this.dao.save(r);
        this.dao.load();
        assertNotNull(this.dao.findServiceById(r.getId()));
    }

    @Test
    public void checkNullabilityOfAccessStrategy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId(HTTPS_SERVICE_ID);
        r.setName("checkNullabilityOfAccessStrategy");
        r.setId(43210);
        r.setAccessStrategy(null);
        this.dao.save(r);
        this.dao.load();
        final RegisteredService s = this.dao.findServiceById(43210);
        assertNotNull(s);
        assertNotNull(s.getAccessStrategy());
    }

    @Test
    public void verifyMappedRegexAttributeFilter() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("something");
        r.setName("verifyMappedRegexAttributeFilter");
        r.setId(4245);

        final ReturnAllowedAttributeReleasePolicy p = new ReturnAllowedAttributeReleasePolicy();
        final RegisteredServiceMappedRegexAttributeFilter filter = new RegisteredServiceMappedRegexAttributeFilter();
        filter.setCompleteMatch(true);
        filter.setPatterns(CollectionUtils.wrap("one", "two"));
        p.setAttributeFilter(filter);

        r.setAttributeReleasePolicy(p);
        this.dao.save(r);
        this.dao.load();
    }

    @Test
    public void verifyServiceContacts() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId("verifyServiceContacts");
        r.setName("verifyServiceContacts");
        r.setId(5000);

        final DefaultRegisteredServiceContact contact = new DefaultRegisteredServiceContact();
        contact.setDepartment("Department");
        contact.setEmail("cas@example.org");
        contact.setName("Contact");
        contact.setPhone("123-456-7890");
        r.setContacts(CollectionUtils.wrap(contact));
        this.dao.save(r);
        this.dao.load();
    }
    
    @Test
    public void persistCustomServiceProperties() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setServiceId(HTTPS_SERVICE_ID);
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
        this.dao.load();
        assertNotNull(this.dao.findServiceById(r.getId()));
        assertEquals(r.getProperties().size(), 2);
        assertNotNull(r.getProperties().get("field1"));

        final RegisteredServiceProperty prop = r.getProperties().get("field1");
        assertEquals(prop.getValues().size(), 2);
    }
}
