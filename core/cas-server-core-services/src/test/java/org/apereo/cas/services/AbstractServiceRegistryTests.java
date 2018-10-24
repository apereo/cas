package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import com.google.common.collect.ArrayListMultimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTimeUtils;
import org.jooq.lambda.Unchecked;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Abstracted service registry tests for all implementations.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractServiceRegistryTests {
    public static final int LOAD_SIZE = 1;

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    private final Class<? extends RegisteredService> registeredServiceClass;

    private ServiceRegistry serviceRegistry;

    @BeforeEach
    public void setUp() {
        this.serviceRegistry = getNewServiceRegistry();
        clearServiceRegistry();
        initializeServiceRegistry();
    }

    @AfterEach
    public void tearDown() {
        clearServiceRegistry();
        tearDownServiceRegistry();
    }

    /**
     * Abstract method to retrieve a new service registry. Implementing classes
     * return the ServiceRegistry they wish to test.
     *
     * @return the ServiceRegistry we wish to test
     */
    protected abstract ServiceRegistry getNewServiceRegistry();

    @Test
    public void verifyEmptyRegistry() {
        assertEquals("Loaded too many", 0, serviceRegistry.load().size());
        assertEquals("Counted too many", 0, serviceRegistry.size());
    }

    @Test
    public void verifySave() {
        val svc = buildRegisteredServiceInstance(RandomUtils.nextInt());
        assertEquals(serviceRegistry.save(svc).getServiceId(), svc.getServiceId());
    }

    @Test
    public void verifySaveAndLoad() {
        for (int i = 0; i < getLoadSize(); i++) {
            val svc = buildRegisteredServiceInstance(i);
            this.serviceRegistry.save(svc);
            val svc2 = this.serviceRegistry.findServiceByExactServiceName(svc.getName());
            assertNotNull(svc2);
            this.serviceRegistry.delete(svc2);
        }
        assertTrue(this.serviceRegistry.load().isEmpty());
    }

    @Test
    public void verifyNonExistingService() {
        assertNull(this.serviceRegistry.findServiceById(9999991));
        assertNull(this.serviceRegistry.findServiceById("9999991"));
    }

    @Test
    public void verifySavingServices() {
        this.serviceRegistry.save(buildRegisteredServiceInstance(100));
        val services = this.serviceRegistry.load();
        assertEquals(1, services.size());
        assertEquals(1, serviceRegistry.size());
        this.serviceRegistry.save(buildRegisteredServiceInstance(101));
        val services2 = this.serviceRegistry.load();
        assertEquals(2, services2.size());
        assertEquals(2, serviceRegistry.size());
    }

    @Test
    public void verifyUpdatingServices() {
        this.serviceRegistry.save(buildRegisteredServiceInstance(200));
        val services = this.serviceRegistry.load();
        assertFalse(services.isEmpty());
        val rs = (AbstractRegisteredService) this.serviceRegistry.findServiceById(services.stream().findFirst().orElse(null).getId());
        assertNotNull(rs);
        rs.setEvaluationOrder(9999);
        rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        rs.setName("Another Test Service");
        rs.setDescription("The new description");
        rs.setServiceId("https://hello.world");
        rs.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https"));
        rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
        assertNotNull(this.serviceRegistry.save(rs));

        val rs3 = this.serviceRegistry.findServiceById(rs.getId());
        assertEquals(rs3.getName(), rs.getName());
        assertEquals(rs3.getDescription(), rs.getDescription());
        assertEquals(rs3.getEvaluationOrder(), rs.getEvaluationOrder());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getProxyPolicy(), rs.getProxyPolicy());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getServiceId(), rs.getServiceId());
    }

    @Test
    public void verifyDeletingSingleService() {
        val rs = buildRegisteredServiceInstance(300);
        val rs2 = buildRegisteredServiceInstance(301);
        this.serviceRegistry.save(rs2);
        this.serviceRegistry.save(rs);
        this.serviceRegistry.load();
        this.serviceRegistry.delete(rs2);

        assertFalse(this.serviceRegistry.load().isEmpty());

        val rsNew = this.serviceRegistry.findServiceByExactServiceName(rs.getName());
        assertNotNull(rsNew);
    }

    @Test
    public void verifyDeletingServices() {
        this.serviceRegistry.save(buildRegisteredServiceInstance(400));
        this.serviceRegistry.save(buildRegisteredServiceInstance(401));
        val services = this.serviceRegistry.load();
        services.forEach(registeredService -> this.serviceRegistry.delete(registeredService));
        assertEquals(0, this.serviceRegistry.load().size());
    }

    @Test
    public void verifyExpiredServiceDeleted() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDateTime.now().minusSeconds(1)));
        val r2 = this.serviceRegistry.save(r);
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis() + 2000);
        this.serviceRegistry.load();
        val svc = this.serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc);
    }

    @Test
    public void verifyExpiredServiceDisabled() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val expirationDate = LocalDateTime.now().plusSeconds(1);
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false, expirationDate));
        val r2 = this.serviceRegistry.save(r);
        val svc = this.serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc);
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis() + 2000);
        val svc2 = this.serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc2);
    }

    @Test
    public void checkSaveMethodWithNonExistentServiceAndNoAttributes() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(this.serviceRegistry.findServiceByExactServiceId(r.getServiceId()));
        assertNotNull(this.serviceRegistry.findServiceByExactServiceName(r.getName()));
    }

    @Test
    public void checkSaveMethodWithDelegatedAuthnPolicy() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy(CollectionUtils.wrapList("one", "two")));
        r.setAccessStrategy(strategy);
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveWithAuthnMethodPolicy() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val policy =
            new DefaultRegisteredServiceMultifactorPolicy();
        policy.setFailureMode(RegisteredServiceMultifactorPolicy.FailureModes.PHANTOM);

        val set = new HashSet<String>();
        set.add("duoAuthenticationProvider");
        policy.setMultifactorAuthenticationProviders(set);
        policy.setPrincipalAttributeNameTrigger("memberOf");
        policy.setPrincipalAttributeValueToMatch("cas|CAS|admin");
        r.setMultifactorPolicy(policy);
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveMethodWithDefaultUsernameAttribute() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveMethodWithConsentPolicy() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("test"),
            CollectionUtils.wrapSet("test")));
        r.setAttributeReleasePolicy(policy);
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void ensureSaveMethodWithDefaultPrincipalAttribute() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn", "UPPER"));
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveMethodWithDefaultAnonymousAttribute() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator("helloworld")
        ));
        val r2 = this.serviceRegistry.save(r);
        this.serviceRegistry.load();
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        val anon =
            (AnonymousRegisteredServiceUsernameAttributeProvider) r3.getUsernameAttributeProvider();
        val ss =
            (ShibbolethCompatiblePersistentIdGenerator) anon.getPersistentIdGenerator();
        assertEquals("helloworld", ss.getSalt());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyServiceExpirationPolicy() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDate.now()));
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(r3.getExpirationPolicy());
        assertEquals(r2.getExpirationPolicy(), r3.getExpirationPolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicy() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveMethodWithExistingServiceNoAttribute() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        this.serviceRegistry.save(r);
        r.setTheme("mytheme");

        this.serviceRegistry.save(r);
        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveAttributeReleasePolicyMappingRules() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val map = ArrayListMultimap.<String, Object>create();
        map.put("attr1", "newattr1");
        map.put("attr2", "newattr2");
        map.put("attr2", "newattr3");

        val policy = new ReturnMappedAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap(map));
        r.setAttributeReleasePolicy(policy);

        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRules() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);

        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        r.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https://.+"));
        r.setRequiredHandlers(CollectionUtils.wrapHashSet("h1", "h2"));

        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);
        r.getAttributeReleasePolicy().setAttributeFilter(new RegisteredServiceRegexAttributeFilter("\\w+"));

        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifyServiceType() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val r2 = this.serviceRegistry.save(r);
        assertTrue(r2 instanceof RegexRegisteredService);
    }

    @Test
    @SneakyThrows
    public void verifyServiceRemovals() {
        val list = new ArrayList<RegisteredService>(5);
        IntStream.range(1, 5).forEach(i -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
            list.add(this.serviceRegistry.save(r));
        });

        list.forEach(Unchecked.consumer(r2 -> {
            Thread.sleep(500);
            this.serviceRegistry.delete(r2);
            Thread.sleep(2000);
            assertNull(this.serviceRegistry.findServiceById(r2.getId()));
        }));
    }

    @Test
    public void checkForAuthorizationStrategy() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val authz = new DefaultRegisteredServiceAccessStrategy(false, false);

        val attrs = new HashMap<String, Set<String>>();
        attrs.put("cn", Collections.singleton("v1, v2, v3"));
        attrs.put("memberOf", Collections.singleton("v4, v5, v6"));
        authz.setRequiredAttributes(attrs);
        r.setAccessStrategy(authz);

        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyAccessStrategyWithStarEndDate() throws Exception {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val authz =
            new TimeBasedRegisteredServiceAccessStrategy(true, false);

        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(10).toString());

        authz.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        r.setAccessStrategy(authz);

        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyAccessStrategyWithEndpoint() throws Exception {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val authz = new RemoteEndpointServiceAccessStrategy();
        authz.setEndpointUrl("http://www.google.com?this=that");
        authz.setAcceptableResponseCodes("200,405,403");
        authz.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        r.setAccessStrategy(authz);

        val r2 = this.serviceRegistry.save(r);
        val r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void serializePublicKeyForServiceAndVerify() {
        val publicKey = new RegisteredServicePublicKeyImpl("classpath:RSA1024Public.key", "RSA");
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setPublicKey(publicKey);

        this.serviceRegistry.save(r);
        this.serviceRegistry.load();
        assertNotNull(this.serviceRegistry.findServiceByExactServiceName(r.getName()));
    }

    @Test
    public void verifyMappedRegexAttributeFilter() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());

        val p = new ReturnAllowedAttributeReleasePolicy();
        val filter = new RegisteredServiceMappedRegexAttributeFilter();
        filter.setCompleteMatch(true);
        filter.setPatterns(CollectionUtils.wrap("one", "two"));
        p.setAttributeFilter(filter);

        r.setAttributeReleasePolicy(p);
        this.serviceRegistry.save(r);
        this.serviceRegistry.load();
    }

    @Test
    public void verifyServiceContacts() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        val contact = new DefaultRegisteredServiceContact();
        contact.setDepartment("Department");
        contact.setEmail("cas@example.org");
        contact.setName("Contact");
        contact.setPhone("123-456-7890");
        r.setContacts(CollectionUtils.wrap(contact));
        this.serviceRegistry.save(r);
        this.serviceRegistry.load();
    }

    @Test
    public void persistCustomServiceProperties() {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt());

        val properties = new HashMap<String, RegisteredServiceProperty>();
        val property = new DefaultRegisteredServiceProperty();
        val values = new HashSet<String>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        properties.put("field1", property);

        val property2 = new DefaultRegisteredServiceProperty();
        val values2 = new HashSet<String>();
        values2.add("value12");
        values2.add("value22");
        property2.setValues(values2);
        properties.put("field2", property2);

        r.setProperties(properties);

        this.serviceRegistry.save(r);
        this.serviceRegistry.load();
        assertNotNull(this.serviceRegistry.findServiceByExactServiceName(r.getName()));
        assertEquals(2, r.getProperties().size());
        assertNotNull(r.getProperties().get("field1"));

        val prop = r.getProperties().get("field1");
        assertEquals(2, prop.getValues().size());
    }

    /**
     * Method to mock RegisteredService objects for testing.
     *
     * @param randomId addition to service name for uniqueness.
     * @return new registered service object
     */
    protected AbstractRegisteredService buildRegisteredServiceInstance(final int randomId) {
        val id = String.format("^http://www.serviceid%s.org", randomId);
        val rs = RegisteredServiceTestUtils.getRegisteredService(id, this.registeredServiceClass);
        initializeServiceInstance(rs);
        return rs;
    }

    /**
     * Method to prepare registered service for testing.
     * Implementing classes may override this if more is necessary.
     */
    protected AbstractRegisteredService initializeServiceInstance(final AbstractRegisteredService rs) {
        val propertyMap = new HashMap<String, RegisteredServiceProperty>();
        val property = new DefaultRegisteredServiceProperty();
        val values = new HashSet<String>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        propertyMap.put("field1", property);
        rs.setProperties(propertyMap);
        return rs;
    }

    protected int getLoadSize() {
        return LOAD_SIZE;
    }

    /**
     * Method to prepare the service registry for testing.
     * Implementing classes may override this if more is necessary.
     */
    protected void initializeServiceRegistry() {
    }

    /**
     * Method to shut down the service registry after testing.
     * Implementing classes may override this if more is necessary.
     */
    protected void tearDownServiceRegistry() {
    }

    protected void clearServiceRegistry() {
        val results = this.getServiceRegistry().load();
        results.forEach(service -> this.getServiceRegistry().delete(service));
    }
}
