package org.apereo.cas.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.joda.time.DateTimeUtils;
import org.jooq.lambda.Unchecked;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Abstracted service registry tests for all implementations.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractServiceRegistryTests {
    public static final int LOAD_SIZE = 1;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServiceRegistry serviceRegistry;

    private final Class<? extends RegisteredService> registeredServiceClass;

    @Before
    public void setUp() {
        this.serviceRegistry = getNewServiceRegistry();
        clearServiceRegistry();
        initializeServiceRegistry();
    }

    @After
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
        final var results = this.serviceRegistry.load();
        assertEquals(0, results.size());
    }

    @Test
    public void verifySave() {
        final var svc = buildRegisteredServiceInstance(RandomUtils.nextInt());
        assertEquals(serviceRegistry.save(svc).getServiceId(), svc.getServiceId());
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        for (var i = 0; i < getLoadSize(); i++) {
            final RegisteredService svc = buildRegisteredServiceInstance(i);
            list.add(svc);
            this.serviceRegistry.save(svc);
            final var svc2 = this.serviceRegistry.findServiceByExactServiceName(svc.getName());
            assertNotNull(svc2);
            this.serviceRegistry.delete(svc2);
        }
        assertTrue(this.serviceRegistry.load().isEmpty());
    }

    @Test
    public void verifyNonExistingService() {
        assertNull(this.serviceRegistry.findServiceById(9999991));
    }

    @Test
    public void verifySavingServices() {
        this.serviceRegistry.save(buildRegisteredServiceInstance(100));
        var services = this.serviceRegistry.load();
        assertEquals(1, services.size());
        this.serviceRegistry.save(buildRegisteredServiceInstance(101));
        services = this.serviceRegistry.load();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyUpdatingServices() {
        this.serviceRegistry.save(buildRegisteredServiceInstance(200));
        final var services = this.serviceRegistry.load();
        assertFalse(services.isEmpty());
        final var rs = (AbstractRegisteredService) this.serviceRegistry.findServiceById(services.get(0).getId());
        assertNotNull(rs);
        rs.setEvaluationOrder(9999);
        rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        rs.setName("Another Test Service");
        rs.setDescription("The new description");
        rs.setServiceId("https://hello.world");
        rs.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https"));
        rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
        assertNotNull(this.serviceRegistry.save(rs));

        final var rs3 = this.serviceRegistry.findServiceById(rs.getId());
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
        final RegisteredService rs = buildRegisteredServiceInstance(300);
        final RegisteredService rs2 = buildRegisteredServiceInstance(301);
        this.serviceRegistry.save(rs2);
        this.serviceRegistry.save(rs);
        this.serviceRegistry.load();
        this.serviceRegistry.delete(rs2);

        assertFalse(this.serviceRegistry.load().isEmpty());

        final var rsNew = this.serviceRegistry.findServiceByExactServiceName(rs.getName());
        assertNotNull(rsNew);
    }

    @Test
    public void verifyDeletingServices() {
        this.serviceRegistry.save(buildRegisteredServiceInstance(400));
        this.serviceRegistry.save(buildRegisteredServiceInstance(401));
        final var services = this.serviceRegistry.load();
        services.forEach(registeredService -> this.serviceRegistry.delete(registeredService));
        assertEquals(0, this.serviceRegistry.load().size());
    }

    @Test
    public void verifyExpiredServiceDeleted() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDateTime.now().minusSeconds(1)));
        final var r2 = this.serviceRegistry.save(r);
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis() + 2000);
        this.serviceRegistry.load();
        final var svc = this.serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc);
    }

    @Test
    public void verifyExpiredServiceDisabled() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var expirationDate = LocalDateTime.now().plusSeconds(1);
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false, expirationDate));
        final var r2 = this.serviceRegistry.save(r);
        var svc = this.serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc);
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis() + 2000);
        svc = this.serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc);
    }

    @Test
    public void checkLoadingOfServiceFiles() {
        verifySaveAttributeReleasePolicyMappingRules();
        verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter();
        assertEquals(2, this.serviceRegistry.load().size());
    }

    @Test
    public void checkSaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegisteredService r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void checkSaveMethodWithDelegatedAuthnPolicy() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var strategy = new DefaultRegisteredServiceAccessStrategy();
        strategy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy(CollectionUtils.wrapList("one", "two")));
        r.setAccessStrategy(strategy);
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveWithAuthnMethodPolicy() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var policy =
            new DefaultRegisteredServiceMultifactorPolicy();
        policy.setFailureMode(RegisteredServiceMultifactorPolicy.FailureModes.PHANTOM);

        final Set<String> set = new HashSet<>();
        set.add("duoAuthenticationProvider");
        policy.setMultifactorAuthenticationProviders(set);
        policy.setPrincipalAttributeNameTrigger("memberOf");
        policy.setPrincipalAttributeValueToMatch("cas|CAS|admin");
        r.setMultifactorPolicy(policy);
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveMethodWithDefaultUsernameAttribute() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void execSaveMethodWithConsentPolicy() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("test"),
            CollectionUtils.wrapSet("test")));
        r.setAttributeReleasePolicy(policy);
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void ensureSaveMethodWithDefaultPrincipalAttribute() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("cn", "UPPER"));
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveMethodWithDefaultAnonymousAttribute() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator("helloworld")
        ));
        final var r2 = this.serviceRegistry.save(r);
        this.serviceRegistry.load();
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        final var anon =
            (AnonymousRegisteredServiceUsernameAttributeProvider) r3.getUsernameAttributeProvider();
        final var ss =
            (ShibbolethCompatiblePersistentIdGenerator) anon.getPersistentIdGenerator();
        assertEquals("helloworld", ss.getSalt());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyServiceExpirationPolicy() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDate.now()));
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(r3.getExpirationPolicy());
        assertEquals(r2.getExpirationPolicy(), r3.getExpirationPolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicy() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveMethodWithExistingServiceNoAttribute() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        this.serviceRegistry.save(r);
        r.setTheme("mytheme");

        this.serviceRegistry.save(r);
        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifySaveAttributeReleasePolicyMappingRules() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final Multimap<String, Object> map = ArrayListMultimap.create();
        map.put("attr1", "newattr1");
        map.put("attr2", "newattr2");
        map.put("attr2", "newattr3");

        final var policy = new ReturnMappedAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap(map));
        r.setAttributeReleasePolicy(policy);

        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRules() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);

        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        r.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https://.+"));
        r.setRequiredHandlers(CollectionUtils.wrapHashSet("h1", "h2"));

        final var policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);
        r.getAttributeReleasePolicy().setAttributeFilter(new RegisteredServiceRegexAttributeFilter("\\w+"));

        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifyServiceType() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var r2 = this.serviceRegistry.save(r);
        assertTrue(r2 instanceof RegexRegisteredService);
    }

    @Test
    @SneakyThrows
    public void verifyServiceRemovals() {
        final List<RegisteredService> list = new ArrayList<>(5);
        IntStream.range(1, 5).forEach(i -> {
            final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
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
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var authz =
            new DefaultRegisteredServiceAccessStrategy(false, false);

        final Map<String, Set<String>> attrs = new HashMap<>();
        attrs.put("cn", Collections.singleton("v1, v2, v3"));
        attrs.put("memberOf", Collections.singleton("v4, v5, v6"));
        authz.setRequiredAttributes(attrs);
        r.setAccessStrategy(authz);

        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyAccessStrategyWithStarEndDate() throws Exception {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var authz =
            new TimeBasedRegisteredServiceAccessStrategy(true, false);

        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(10).toString());

        authz.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        r.setAccessStrategy(authz);

        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void verifyAccessStrategyWithEndpoint() throws Exception {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var authz = new RemoteEndpointServiceAccessStrategy();
        authz.setEndpointUrl("http://www.google.com?this=that");
        authz.setAcceptableResponseCodes("200,405,403");
        authz.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        r.setAccessStrategy(authz);

        final var r2 = this.serviceRegistry.save(r);
        final var r3 = this.serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @Test
    public void serializePublicKeyForServiceAndVerify() {
        final RegisteredServicePublicKey publicKey = new RegisteredServicePublicKeyImpl(
            "classpath:RSA1024Public.key", "RSA");
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        r.setPublicKey(publicKey);

        this.serviceRegistry.save(r);
        this.serviceRegistry.load();
        assertNotNull(this.serviceRegistry.findServiceByExactServiceName(r.getName()));
    }

    @Test
    public void verifyMappedRegexAttributeFilter() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());

        final var p = new ReturnAllowedAttributeReleasePolicy();
        final var filter = new RegisteredServiceMappedRegexAttributeFilter();
        filter.setCompleteMatch(true);
        filter.setPatterns(CollectionUtils.wrap("one", "two"));
        p.setAttributeFilter(filter);

        r.setAttributeReleasePolicy(p);
        this.serviceRegistry.save(r);
        this.serviceRegistry.load();
    }

    @Test
    public void verifyServiceContacts() {
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());
        final var contact = new DefaultRegisteredServiceContact();
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
        final var r = buildRegisteredServiceInstance(RandomUtils.nextInt());

        final Map<String, RegisteredServiceProperty> properties = new HashMap<>();
        final var property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        properties.put("field1", property);

        final var property2 = new DefaultRegisteredServiceProperty();
        final Set<String> values2 = new HashSet<>();
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

        final var prop = r.getProperties().get("field1");
        assertEquals(2, prop.getValues().size());
    }

    /**
     * Method to mock RegisteredService objects for testing.
     *
     * @param randomId addition to service name for uniqueness.
     * @return new registered service object
     */
    protected AbstractRegisteredService buildRegisteredServiceInstance(final int randomId) {
        final var id = String.format("^http://www.serviceid%s.org", randomId);
        final var rs = RegisteredServiceTestUtils.getRegisteredService(id, this.registeredServiceClass);
        initializeServiceInstance(rs);
        return rs;
    }

    /**
     * Method to prepare registered service for testing.
     * Implementing classes may override this if more is necessary.
     */
    protected AbstractRegisteredService initializeServiceInstance(final AbstractRegisteredService rs) {
        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();
        final var property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
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
        final var results = this.getServiceRegistry().load();
        results.forEach(service -> this.getServiceRegistry().delete(service));
    }
}
