package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.google.common.collect.ArrayListMultimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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

    private ServiceRegistry serviceRegistry;

    protected static BaseRegisteredService buildRegisteredServiceInstance(
        final long randomId,
        final Class<? extends BaseWebBasedRegisteredService> registeredServiceClass) {
        val id = String.format("^http://www.serviceid%s.org", randomId);
        val rs = RegisteredServiceTestUtils.getRegisteredService(id, registeredServiceClass);
        initializeServiceInstance(rs);
        return rs;
    }

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
    void setUp() throws Exception {
        serviceRegistry = getNewServiceRegistry();
        clearServiceRegistry();
        initializeServiceRegistry();
    }

    @AfterEach
    public void tearDown() throws Exception {
        clearServiceRegistry();
        tearDownServiceRegistry();
    }

    @RetryingTest(3)
    @Order(1000)
    void verifyEmptyRegistry() {
        serviceRegistry.deleteAll();
        assertEquals(0, serviceRegistry.load().size(), "Loaded too many");
        assertEquals(0, serviceRegistry.size(), "Counted too many");
    }

    @Test
    void verifySave() {
        getRegisteredServiceTypes().forEach(type -> {
            val svc = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            serviceRegistry.save(() -> svc,
                result -> assertEquals(result.getServiceId(), svc.getServiceId(), type::getName),
                1);
        });
    }

    @Test
    void verifySaveAndLoad() {
        getRegisteredServiceTypes().forEach(type -> {
            serviceRegistry.deleteAll();
            val loadSize = getLoadSize();
            for (var i = 0; i < loadSize; i++) {
                val svc = buildRegisteredServiceInstance(i, type)
                    .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
                serviceRegistry.save(svc);

                val svc2 = serviceRegistry.findServiceByExactServiceName(svc.getName());
                assertNotNull(svc2, type::getName);

                val svc3 = serviceRegistry.findServiceById(svc2.getId());
                assertEquals(svc2, svc3);

                serviceRegistry.delete(svc2);
            }
            val results = serviceRegistry.load();
            assertTrue(results.isEmpty());
            assertEquals(0, serviceRegistry.getServicesStream().count());
            assertEquals(0, serviceRegistry.size());
        });
    }

    @Test
    void verifyNonExistingService() {
        assertNull(serviceRegistry.findServiceById(9999991));
    }

    @Test
    void verifySavingServices() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(100, type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val savedService = serviceRegistry.save(registeredService);
            val services = serviceRegistry.load();
            assertTrue(services.stream().anyMatch(svc -> svc.equals(savedService)));
            val registeredService2 = buildRegisteredServiceInstance(101, type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val savedService2 = serviceRegistry.save(registeredService2);
            val services2 = serviceRegistry.load();
            assertTrue(services2.stream().anyMatch(svc -> svc.equals(savedService2)));
        });
    }

    @Test
    void verifyUpdatingServices() {
        getRegisteredServiceTypes().forEach(type -> {
            val toSave = buildRegisteredServiceInstance(200, type).setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            serviceRegistry.save(toSave);
            val services = serviceRegistry.load();
            assertFalse(services.isEmpty());
            val rs = (BaseRegisteredService) serviceRegistry.findServiceById(services.iterator().next().getId());
            assertNotNull(rs, type::getName);
            rs.setEvaluationOrder(9999);
            rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
            rs.setDescription("The new description");
            rs.setServiceId("https://hello.world");
            rs.setTheme("some-theme");

            if (rs instanceof final CasRegisteredService cas) {
                val policy = new RegexMatchingRegisteredServiceProxyPolicy();
                policy.setPattern("https");
                cas.setProxyPolicy(policy);
            }
            rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
            assertNotNull(serviceRegistry.save(rs), type::getName);

            val rs3 = (WebBasedRegisteredService) serviceRegistry.findServiceById(rs.getId());
            assertEquals(rs3.getDescription(), rs.getDescription());
            assertEquals(rs3.getEvaluationOrder(), rs.getEvaluationOrder());
            assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
            assertEquals(rs3.getServiceId(), rs.getServiceId());
            assertEquals(rs3.getTheme(), rs.getTheme());

            val rs4 = serviceRegistry.findServicePredicate(registeredService -> registeredService.getId() == rs.getId());
            assertTrue(rs4.stream().map(rs5 -> rs5.getName().equals(rs.getName())).findFirst().isPresent());
        });
    }

    @Test
    void verifyDeletingSingleService() {
        getRegisteredServiceTypes().forEach(type -> {
            val rs = buildRegisteredServiceInstance(300, type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val rs2 = buildRegisteredServiceInstance(301, type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            serviceRegistry.save(rs2);
            serviceRegistry.save(rs);
            serviceRegistry.load();
            serviceRegistry.delete(rs2);

            assertFalse(serviceRegistry.load().isEmpty());

            val rsNew = serviceRegistry.findServiceByExactServiceName(rs.getName());
            assertNotNull(rsNew);
        });
    }

    @Test
    void verifyDeletingServices() {
        getRegisteredServiceTypes().forEach(type -> {
            serviceRegistry.save(buildRegisteredServiceInstance(400, type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE));
            serviceRegistry.save(buildRegisteredServiceInstance(401, type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE));
            val services = serviceRegistry.load();
            services.forEach(registeredService -> serviceRegistry.delete(registeredService));
            assertEquals(0, serviceRegistry.load().size());
        });
    }

    @RetryingTest(2)
    void verifyExpiredServiceDeleted() {
        getRegisteredServiceTypes().forEach(type -> {
            val service = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(1)));
            val savedService = serviceRegistry.save(service);
            serviceRegistry.load();
            await().untilAsserted(() -> assertNotNull(serviceRegistry.findServiceByExactServiceName(savedService.getName())));
        });
    }

    @Test
    void verifyServiceLookupByServiceId() {
        getRegisteredServiceTypes().forEach(type -> {
            val r1 = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val id = UUID.randomUUID().toString();
            r1.setServiceId(".*%s.*".formatted(id));
            r1.setEvaluationOrder(100);
            serviceRegistry.save(r1);

            val r2 = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            r2.setServiceId(r1.getServiceId());
            r2.setEvaluationOrder(1);
            serviceRegistry.save(r2);
            val svc = serviceRegistry.findServiceBy(id);
            assertNotNull(svc);
            assertEquals(r2, svc);
            assertNull(serviceRegistry.findServiceBy("this-service-id-does-not-exist"));
        });
    }

    @Test
    void verifyExpiredServiceDisabled() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val expirationDate = LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(1);
            r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false, expirationDate));
            val r2 = serviceRegistry.save(r);
            val svc = serviceRegistry.findServiceByExactServiceName(r2.getName());
            assertNotNull(svc, () -> "1: " + type.getName());
            val svc2 = serviceRegistry.findServiceByExactServiceName(r2.getName());
            assertNotNull(svc2, () -> "2: " + type.getName());
        });
    }

    @Test
    void checkSaveMethodWithNonExistentServiceAndNoAttributes() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
            assertNotNull(serviceRegistry.findServiceByExactServiceId(r.getServiceId()));
            assertNotNull(serviceRegistry.findServiceByExactServiceName(r.getName()));
        });
    }

    @Test
    void checkSaveMethodWithDelegatedAuthnPolicy() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val strategy = new DefaultRegisteredServiceAccessStrategy();
            val providers = CollectionUtils.wrapList("one", "two");
            strategy.setDelegatedAuthenticationPolicy(new DefaultRegisteredServiceDelegatedAuthenticationPolicy(providers, true, false, null));
            r.setAccessStrategy(strategy);
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void execSaveWithAuthnMethodPolicy() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val policy = new DefaultRegisteredServiceMultifactorPolicy();
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
        });
    }

    @Test
    void execSaveMethodWithDefaultUsernameAttribute() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            r.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void execSaveMethodWithConsentPolicy() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val policy = new ReturnAllAttributeReleasePolicy();
            policy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("test"),
                CollectionUtils.wrapSet("test")));
            r.setAttributeReleasePolicy(policy);
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void ensureSaveMethodWithDefaultPrincipalAttribute() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);

            val provider = new PrincipalAttributeRegisteredServiceUsernameProvider();
            provider.setCanonicalizationMode("UPPER");
            provider.setUsernameAttribute("cn");
            r.setUsernameAttributeProvider(provider);
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void verifySaveMethodWithDefaultAnonymousAttribute() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
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
        });
    }

    @Test
    void verifyServiceExpirationPolicy() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            r.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true, LocalDate.now(ZoneId.systemDefault()).toString()));
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
            assertNotNull(r3.getExpirationPolicy());
            assertEquals(r2.getExpirationPolicy(), r3.getExpirationPolicy());
        });
    }

    @Test
    void verifySaveAttributeReleasePolicy() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
            assertNotNull(r3.getAttributeReleasePolicy());
            assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
        });
    }

    @Test
    void verifySaveMethodWithExistingServiceNoAttribute() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            serviceRegistry.save(r);
            r.setTheme("mytheme");

            serviceRegistry.save(r);
            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void verifySaveAttributeReleasePolicyMappingRules() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
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
        });
    }

    @Test
    void verifySaveAttributeReleasePolicyAllowedAttrRules() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
            r.setAttributeReleasePolicy(policy);

            val r2 = serviceRegistry.save(r);
            val r3 = serviceRegistry.findServiceById(r2.getId());

            assertEquals(r2, r3);
            assertNotNull(r3.getAttributeReleasePolicy());
            assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
        });
    }

    @Test
    void verifySaveAttributeReleasePolicyAllowedAttrRulesAndFilter() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));

            if (registeredService instanceof final CasRegisteredService casRegisteredService) {
                val policy = new RegexMatchingRegisteredServiceProxyPolicy();
                policy.setPattern("https");
                casRegisteredService.setProxyPolicy(policy);
            }
            registeredService.getAuthenticationPolicy().getRequiredAuthenticationHandlers().addAll(CollectionUtils.wrapHashSet("h1", "h2"));

            val policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setAllowedAttributes(Arrays.asList("1", "2", "3"));
            registeredService.setAttributeReleasePolicy(policy);
            registeredService.getAttributeReleasePolicy().setAttributeFilter(new RegisteredServiceRegexAttributeFilter("\\w+"));

            val r2 = serviceRegistry.save(registeredService);
            val r3 = serviceRegistry.findServiceById(r2.getId());

            assertEquals(r2, r3);
            assertNotNull(r3.getAttributeReleasePolicy());
            assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
        });
    }

    @Test
    void verifyServiceType() {
        getRegisteredServiceTypes().forEach(type -> {
            val r = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val r2 = serviceRegistry.save(r);
            assertSame(r2.getClass(), type);
        });
    }

    @Test
    void verifyServiceRemovals() {
        getRegisteredServiceTypes().forEach(type -> {
            val list = IntStream.range(1, 5)
                .mapToObj(i -> buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                    .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE))
                .map(r -> serviceRegistry.save(r))
                .toList();

            list.forEach(Unchecked.consumer(r2 -> {
                Thread.sleep(1000);
                assertTrue(serviceRegistry.delete(r2));
                Thread.sleep(2000);
                assertNull(serviceRegistry.findServiceById(r2.getId()));
            }));
        });
    }

    @Test
    void checkForAuthorizationStrategy() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val authz = new DefaultRegisteredServiceAccessStrategy(false, false);

            val attrs = new HashMap<String, Set<String>>();
            attrs.put("cn", Set.of("v1, v2, v3"));
            attrs.put("memberOf", Set.of("v4, v5, v6"));
            authz.setRequiredAttributes(attrs);
            registeredService.setAccessStrategy(authz);

            val r2 = serviceRegistry.save(registeredService);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void verifyAccessStrategyWithStarEndDate() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val authz = new TimeBasedRegisteredServiceAccessStrategy();
            authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
            authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(10).toString());
            registeredService.setAccessStrategy(authz);

            val r2 = serviceRegistry.save(registeredService);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void verifyAccessStrategyWithEndpoint() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val authz = new RemoteEndpointServiceAccessStrategy();
            authz.setEndpointUrl("http://www.google.com?this=that");
            authz.setAcceptableResponseCodes("200,405,403");
            registeredService.setAccessStrategy(authz);

            val r2 = serviceRegistry.save(registeredService);
            val r3 = serviceRegistry.findServiceById(r2.getId());
            assertEquals(r2, r3);
        });
    }

    @Test
    void serializePublicKeyForServiceAndVerify() {
        getRegisteredServiceTypes().forEach(type -> {
            val publicKey = new RegisteredServicePublicKeyImpl("classpath:RSA1024Public.key", "RSA");
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            registeredService.setPublicKey(publicKey);

            serviceRegistry.save(registeredService);
            serviceRegistry.load();
            assertNotNull(serviceRegistry.findServiceByExactServiceName(registeredService.getName()));
        });
    }

    @Test
    void verifyMappedRegexAttributeFilter() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);

            val releasePolicy = new ReturnAllowedAttributeReleasePolicy();
            val filter = new RegisteredServiceMappedRegexAttributeFilter();
            filter.setCompleteMatch(true);
            filter.setPatterns(CollectionUtils.wrap("one", "two"));
            releasePolicy.setAttributeFilter(filter);

            registeredService.setAttributeReleasePolicy(releasePolicy);
            serviceRegistry.save(registeredService);
            serviceRegistry.load();
        });
    }

    @Test
    void verifyServiceContacts() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
            val contact = new DefaultRegisteredServiceContact();
            contact.setDepartment("Department");
            contact.setEmail("cas@example.org");
            contact.setName("Contact");
            contact.setPhone("123-456-7890");
            registeredService.setContacts(CollectionUtils.wrap(contact));
            serviceRegistry.save(registeredService);
            serviceRegistry.load();
            serviceRegistry.delete(registeredService);
        });
    }

    @Test
    void persistCustomServiceProperties() {
        getRegisteredServiceTypes().forEach(type -> {
            val registeredService = buildRegisteredServiceInstance(RandomUtils.nextInt(), type)
                .setId(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
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

            registeredService.setProperties(properties);

            serviceRegistry.save(registeredService);
            serviceRegistry.load();
            assertNotNull(serviceRegistry.findServiceByExactServiceName(registeredService.getName()));
            assertEquals(2, registeredService.getProperties().size());
            assertNotNull(registeredService.getProperties().get("field1"));

            val prop = registeredService.getProperties().get("field1");
            assertEquals(2, prop.getValues().size());
            serviceRegistry.delete(registeredService);
        });
    }

    protected abstract ServiceRegistry getNewServiceRegistry() throws Exception;

    protected void initializeServiceRegistry() {
    }

    protected void tearDownServiceRegistry() throws Exception {
    }

    protected void clearServiceRegistry() {
        getServiceRegistry().deleteAll();
        assertTrue(getServiceRegistry().load().isEmpty());
    }

    protected Stream<Class<? extends BaseWebBasedRegisteredService>> getRegisteredServiceTypes() {
        return Stream.of(CasRegisteredService.class);
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }

}
