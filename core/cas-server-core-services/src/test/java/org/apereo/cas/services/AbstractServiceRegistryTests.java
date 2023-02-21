package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import com.google.common.collect.ArrayListMultimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.RetryingTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstracted service registry tests for all implementations.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractServiceRegistryTests {
    public static final int LOAD_SIZE = 1;

    public static final String GET_PARAMETERS = "getParameters";

    private ServiceRegistry serviceRegistry;

    public static Stream<Class<? extends RegisteredService>> getParameters() {
        return Stream.of(
            CasRegisteredService.class,
            OAuthRegisteredService.class,
            SamlRegisteredService.class,
            OidcRegisteredService.class,
            WSFederationRegisteredService.class
        );
    }

    /**
     * Method to mock RegisteredService objects for testing.
     *
     * @param randomId addition to service name for uniqueness.
     * @return new registered service object
     */
    protected static BaseWebBasedRegisteredService buildRegisteredServiceInstance(
        final long randomId,
        final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val id = String.format("^http://www.serviceid%s.org", randomId);
        val rs = RegisteredServiceTestUtils.getRegisteredService(id, registeredServiceClass);
        initializeServiceInstance(rs);
        return rs;
    }

    /**
     * Method to prepare registered service for testing.
     * Implementing classes may override this if more is necessary.
     */
    protected static BaseRegisteredService initializeServiceInstance(final BaseRegisteredService rs) {
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

    protected static int getLoadSize() {
        return LOAD_SIZE;
    }

    @BeforeEach
    public void setUp() {
        serviceRegistry = getNewServiceRegistry();
        clearServiceRegistry();
        initializeServiceRegistry();
    }

    @AfterEach
    public void tearDown() {
        clearServiceRegistry();
        tearDownServiceRegistry();
    }

    @RetryingTest(3)
    @Order(1000)
    public void verifyEmptyRegistry() {
        serviceRegistry.deleteAll();
        assertEquals(0, serviceRegistry.load().size(), "Loaded too many");
        assertEquals(0, serviceRegistry.size(), "Counted too many");
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySave(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val svc = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        serviceRegistry.save(() -> svc,
            result -> assertEquals(result.getServiceId(), svc.getServiceId(), registeredServiceClass::getName),
            1);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySaveAndLoad(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        serviceRegistry.deleteAll();
        for (var i = 0; i < getLoadSize(); i++) {
            val svc = buildRegisteredServiceInstance(i, registeredServiceClass);
            serviceRegistry.save(svc);

            val svc2 = serviceRegistry.findServiceByExactServiceName(svc.getName());
            assertNotNull(svc2, registeredServiceClass::getName);

            val svc3 = serviceRegistry.findServiceById(svc2.getId());
            assertEquals(svc2, svc3);

            serviceRegistry.delete(svc2);
        }
        val results = serviceRegistry.load();
        assertTrue(results.isEmpty());
        assertEquals(0, serviceRegistry.getServicesStream().count());
        assertEquals(0, serviceRegistry.size());
    }

    @Test
    public void verifyNonExistingService() {
        assertNull(serviceRegistry.findServiceById(9999991));
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySavingServices(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        serviceRegistry.save(buildRegisteredServiceInstance(100, registeredServiceClass));
        val services = serviceRegistry.load();
        assertEquals(1, services.size(), registeredServiceClass::getName);
        assertEquals(1, serviceRegistry.size(), registeredServiceClass::getName);
        serviceRegistry.save(buildRegisteredServiceInstance(101, registeredServiceClass));
        val services2 = serviceRegistry.load();
        assertEquals(2, services2.size(), registeredServiceClass::getName);
        assertEquals(2, serviceRegistry.size(), registeredServiceClass::getName);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyUpdatingServices(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        serviceRegistry.save(buildRegisteredServiceInstance(200, registeredServiceClass));
        val services = serviceRegistry.load();
        assertFalse(services.isEmpty());
        val rs = (BaseRegisteredService) serviceRegistry.findServiceById(services.stream()
            .findFirst().orElse(null).getId());
        assertNotNull(rs, registeredServiceClass::getName);
        rs.setEvaluationOrder(9999);
        rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        rs.setDescription("The new description");
        rs.setServiceId("https://hello.world");
        rs.setTheme("some-theme");

        if (rs instanceof CasRegisteredService) {
            val policy = new RegexMatchingRegisteredServiceProxyPolicy();
            policy.setPattern("https");
            ((CasRegisteredService) rs).setProxyPolicy(policy);
        }
        rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
        assertNotNull(serviceRegistry.save(rs), registeredServiceClass::getName);

        val rs3 = (WebBasedRegisteredService) serviceRegistry.findServiceById(rs.getId());
        assertEquals(rs3.getDescription(), rs.getDescription());
        assertEquals(rs3.getEvaluationOrder(), rs.getEvaluationOrder());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getServiceId(), rs.getServiceId());
        assertEquals(rs3.getTheme(), rs.getTheme());

        val rs4 = serviceRegistry.findServicePredicate(registeredService -> registeredService.getId() == rs.getId());
        assertTrue(rs4.stream().map(rs5 -> rs5.getName().equals(rs.getName())).findFirst().isPresent());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyDeletingSingleService(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val rs = buildRegisteredServiceInstance(300, registeredServiceClass);
        val rs2 = buildRegisteredServiceInstance(301, registeredServiceClass);
        serviceRegistry.save(rs2);
        serviceRegistry.save(rs);
        serviceRegistry.load();
        serviceRegistry.delete(rs2);

        assertFalse(serviceRegistry.load().isEmpty());

        val rsNew = serviceRegistry.findServiceByExactServiceName(rs.getName());
        assertNotNull(rsNew);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyDeletingServices(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        serviceRegistry.save(buildRegisteredServiceInstance(400, registeredServiceClass));
        serviceRegistry.save(buildRegisteredServiceInstance(401, registeredServiceClass));
        val services = serviceRegistry.load();
        services.forEach(registeredService -> serviceRegistry.delete(registeredService));
        assertEquals(0, serviceRegistry.load().size());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyExpiredServiceDeleted(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val service = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(1)));
        val savedService = serviceRegistry.save(service);
        serviceRegistry.load();
        await().untilAsserted(() -> assertNotNull(serviceRegistry.findServiceByExactServiceName(savedService.getName())));
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyServiceLookupByServiceId(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r1 = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r1.setServiceId(".*serviceid.*");
        r1.setEvaluationOrder(100);
        serviceRegistry.save(r1);

        val r2 = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r2.setServiceId(".*serviceid.*");
        r2.setEvaluationOrder(1);
        serviceRegistry.save(r2);

        val svc = serviceRegistry.findServiceBy("serviceid");
        assertNotNull(svc);
        assertEquals(r2, svc);

        assertNull(serviceRegistry.findServiceBy("this-service-id-does-not-exist"));
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyExpiredServiceDisabled(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val expirationDate = LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(1);
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false, expirationDate));
        val r2 = serviceRegistry.save(r);
        val svc = serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc, () -> "1: " + registeredServiceClass.getName());
        val svc2 = serviceRegistry.findServiceByExactServiceName(r2.getName());
        assertNotNull(svc2, () -> "2: " + registeredServiceClass.getName());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void checkSaveMethodWithNonExistentServiceAndNoAttributes(
        final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(serviceRegistry.findServiceByExactServiceId(r.getServiceId()));
        assertNotNull(serviceRegistry.findServiceByExactServiceName(r.getName()));
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void checkSaveMethodWithDelegatedAuthnPolicy(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val strategy = new DefaultRegisteredServiceAccessStrategy();
        val providers = CollectionUtils.wrapList("one", "two");
        strategy.setDelegatedAuthenticationPolicy(new DefaultRegisteredServiceDelegatedAuthenticationPolicy(providers, true, false, null));
        r.setAccessStrategy(strategy);
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void execSaveWithAuthnMethodPolicy(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val policy =
            new DefaultRegisteredServiceMultifactorPolicy();
        policy.setFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.PHANTOM);

        val set = new HashSet<String>();
        set.add("duoAuthenticationProvider");
        policy.setMultifactorAuthenticationProviders(set);
        policy.setPrincipalAttributeNameTrigger("memberOf");
        policy.setPrincipalAttributeValueToMatch("cas|CAS|admin");
        r.setMultifactorAuthenticationPolicy(policy);
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void execSaveMethodWithDefaultUsernameAttribute(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void execSaveMethodWithConsentPolicy(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val policy = new ReturnAllAttributeReleasePolicy();
        policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("test"),
            CollectionUtils.wrapSet("test")));
        r.setAttributeReleasePolicy(policy);
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void ensureSaveMethodWithDefaultPrincipalAttribute(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);

        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode(CaseCanonicalizationMode.UPPER.name());
        provider.setUsernameAttribute("cn");
        r.setUsernameAttributeProvider(provider);
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySaveMethodWithDefaultAnonymousAttribute(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator("helloworld")
        ));
        val r2 = serviceRegistry.save(r);
        serviceRegistry.load();
        val r3 = serviceRegistry.findServiceById(r2.getId());
        val anon = (AnonymousRegisteredServiceUsernameAttributeProvider) r3.getUsernameAttributeProvider();
        val ss = (ShibbolethCompatiblePersistentIdGenerator) anon.getPersistentIdGenerator();
        assertEquals("helloworld", ss.getSalt());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyServiceExpirationPolicy(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDate.now(ZoneId.systemDefault()).toString()));
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(r3.getExpirationPolicy());
        assertEquals(r2.getExpirationPolicy(), r3.getExpirationPolicy());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySaveAttributeReleasePolicy(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySaveMethodWithExistingServiceNoAttribute(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        serviceRegistry.save(r);
        r.setTheme("mytheme");

        serviceRegistry.save(r);
        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySaveAttributeReleasePolicyMappingRules(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val map = ArrayListMultimap.<String, Object>create();
        map.put("attr1", "newattr1");
        map.put("attr2", "newattr2");
        map.put("attr2", "newattr3");

        val policy = new ReturnMappedAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap(map));
        r.setAttributeReleasePolicy(policy);

        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySaveAttributeReleasePolicyAllowedAttrRules(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);

        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));

        if (r instanceof CasRegisteredService) {
            val policy = new RegexMatchingRegisteredServiceProxyPolicy();
            policy.setPattern("https");
            ((CasRegisteredService) r).setProxyPolicy(policy);
        }
        r.getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(CollectionUtils.wrapHashSet("h1", "h2"));

        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
        r.setAttributeReleasePolicy(policy);
        r.getAttributeReleasePolicy().setAttributeFilter(new RegisteredServiceRegexAttributeFilter("\\w+"));

        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());

        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyServiceType(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val r2 = serviceRegistry.save(r);
        assertSame(r2.getClass(), registeredServiceClass);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyServiceRemovals(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val list = new ArrayList<RegisteredService>(5);
        IntStream.range(1, 5).forEach(i -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
            list.add(serviceRegistry.save(r));
        });

        list.forEach(Unchecked.consumer(r2 -> {
            Thread.sleep(500);
            serviceRegistry.delete(r2);
            Thread.sleep(2000);
            assertNull(serviceRegistry.findServiceById(r2.getId()));
        }));
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void checkForAuthorizationStrategy(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val authz = new DefaultRegisteredServiceAccessStrategy(false, false);

        val attrs = new HashMap<String, Set<String>>();
        attrs.put("cn", Collections.singleton("v1, v2, v3"));
        attrs.put("memberOf", Collections.singleton("v4, v5, v6"));
        authz.setRequiredAttributes(attrs);
        r.setAccessStrategy(authz);

        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyAccessStrategyWithStarEndDate(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(10).toString());
        r.setAccessStrategy(authz);

        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyAccessStrategyWithEndpoint(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val authz = new RemoteEndpointServiceAccessStrategy();
        authz.setEndpointUrl("http://www.google.com?this=that");
        authz.setAcceptableResponseCodes("200,405,403");
        r.setAccessStrategy(authz);

        val r2 = serviceRegistry.save(r);
        val r3 = serviceRegistry.findServiceById(r2.getId());
        assertEquals(r2, r3);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void serializePublicKeyForServiceAndVerify(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val publicKey = new RegisteredServicePublicKeyImpl("classpath:RSA1024Public.key", "RSA");
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        r.setPublicKey(publicKey);

        serviceRegistry.save(r);
        serviceRegistry.load();
        assertNotNull(serviceRegistry.findServiceByExactServiceName(r.getName()));
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyMappedRegexAttributeFilter(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);

        val p = new ReturnAllowedAttributeReleasePolicy();
        val filter = new RegisteredServiceMappedRegexAttributeFilter();
        filter.setCompleteMatch(true);
        filter.setPatterns(CollectionUtils.wrap("one", "two"));
        p.setAttributeFilter(filter);

        r.setAttributeReleasePolicy(p);
        serviceRegistry.save(r);
        serviceRegistry.load();
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void verifyServiceContacts(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);
        val contact = new DefaultRegisteredServiceContact();
        contact.setDepartment("Department");
        contact.setEmail("cas@example.org");
        contact.setName("Contact");
        contact.setPhone("123-456-7890");
        r.setContacts(CollectionUtils.wrap(contact));
        serviceRegistry.save(r);
        serviceRegistry.load();
        serviceRegistry.delete(r);
    }

    @ParameterizedTest
    @MethodSource(GET_PARAMETERS)
    public void persistCustomServiceProperties(final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), registeredServiceClass);

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

        serviceRegistry.save(r);
        serviceRegistry.load();
        assertNotNull(serviceRegistry.findServiceByExactServiceName(r.getName()));
        assertEquals(2, r.getProperties().size());
        assertNotNull(r.getProperties().get("field1"));

        val prop = r.getProperties().get("field1");
        assertEquals(2, prop.getValues().size());
        serviceRegistry.delete(r);
    }

    /**
     * Abstract method to retrieve a new service registry. Implementing classes
     * return the ServiceRegistry they wish to test.
     *
     * @return the ServiceRegistry we wish to test
     */
    protected abstract ServiceRegistry getNewServiceRegistry();

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
        getServiceRegistry().deleteAll();
        assertTrue(getServiceRegistry().load().isEmpty());
    }
}
