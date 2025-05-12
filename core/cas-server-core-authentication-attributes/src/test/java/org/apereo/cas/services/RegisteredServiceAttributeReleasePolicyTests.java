package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.MergingPersonAttributeDaoImpl;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.DefaultPrincipalAttributesRepositoryCache;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Attribute filtering policy tests.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
class RegisteredServiceAttributeReleasePolicyTests {

    private static final String ATTR_1 = "attr1";

    private static final String ATTR_2 = "attr2";

    private static final String ATTR_3 = "attr3";

    private static final String VALUE_1 = "value1";

    private static final String VALUE_2 = "value2";

    private static final String NEW_ATTR_1_VALUE = "newAttr1";

    private static final String PRINCIPAL_ID = "principalId";

    @TestConfiguration(value = "CommonTestConfiguration", proxyBeanMethods = false)
    public static class CommonTestConfiguration {
        @Bean
        public PrincipalAttributesRepositoryCache principalAttributesRepositoryCache() {
            return new DefaultPrincipalAttributesRepositoryCache();
        }
    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        CommonTestConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class
    })
    class DefaultTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyMappedAttributeFilterMappedAttributesIsCaseInsensitive() throws Throwable {
            val policy = new ReturnMappedAttributeReleasePolicy();
            val mappedAttr = ArrayListMultimap.<String, Object>create();
            mappedAttr.put(ATTR_1, NEW_ATTR_1_VALUE);
            policy.setAllowedAttributes(CollectionUtils.wrap(mappedAttr));

            val p = mock(Principal.class);
            val map = new HashMap<String, List<Object>>();
            map.put("ATTR1", List.of(VALUE_1));
            when(p.getAttributes()).thenReturn(map);
            when(p.getId()).thenReturn(PRINCIPAL_ID);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(p)
                .build();
            val attr = policy.getAttributes(context);
            assertEquals(1, attr.size());
            assertTrue(attr.containsKey(NEW_ATTR_1_VALUE));

            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .service(CoreAuthenticationTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(p)
                .build();
            val definitions = policy.determineRequestedAttributeDefinitions(releasePolicyContext);
            assertTrue(definitions.containsAll(policy.getAllowedAttributes().keySet()));
        }

        @Test
        void verifyAttributeFilterMappedAttributesIsCaseInsensitive() throws Throwable {
            val policy = new ReturnAllowedAttributeReleasePolicy();
            val attrs = new ArrayList<String>();
            attrs.add(ATTR_1);
            attrs.add(ATTR_2);

            policy.setAllowedAttributes(attrs);

            val p = mock(Principal.class);
            val map = new HashMap<String, List<Object>>();
            map.put("ATTR1", List.of(VALUE_1));
            map.put("ATTR2", List.of(VALUE_2));
            when(p.getAttributes()).thenReturn(map);
            when(p.getId()).thenReturn(PRINCIPAL_ID);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(p)
                .build();
            val attr = policy.getAttributes(context);
            assertEquals(2, attr.size());
            assertTrue(attr.containsKey(ATTR_1));
            assertTrue(attr.containsKey(ATTR_2));
            assertTrue(policy.determineRequestedAttributeDefinitions(context).containsAll(policy.getAllowedAttributes()));
        }

        @Test
        void verifyAttributeFilterMappedAttributes() throws Throwable {
            val policy = new ReturnMappedAttributeReleasePolicy();
            val mappedAttr = ArrayListMultimap.<String, Object>create();
            mappedAttr.put(ATTR_1, NEW_ATTR_1_VALUE);

            policy.setAllowedAttributes(CollectionUtils.wrap(mappedAttr));
            val p = mock(Principal.class);

            val map = new HashMap<String, List<Object>>();
            map.put(ATTR_1, List.of(VALUE_1));
            map.put(ATTR_2, List.of(VALUE_2));
            map.put(ATTR_3, Arrays.asList("v3", "v4"));

            when(p.getAttributes()).thenReturn(map);
            when(p.getId()).thenReturn(PRINCIPAL_ID);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(p)
                .build();
            val attr = policy.getAttributes(context);
            assertEquals(1, attr.size());
            assertTrue(attr.containsKey(NEW_ATTR_1_VALUE));

            val data = SerializationUtils.serialize(policy);
            val p2 = SerializationUtils.deserializeAndCheckObject(data, ReturnMappedAttributeReleasePolicy.class);
            assertNotNull(p2);
            assertEquals(p2.getAllowedAttributes(), policy.getAllowedAttributes());
        }

        @Test
        void verifyServiceAttributeFilterAllowedAttributes() throws Throwable {
            val policy = new ReturnAllowedAttributeReleasePolicy();
            policy.setAllowedAttributes(Arrays.asList(ATTR_1, ATTR_3));
            val mockPrincipal = mock(Principal.class);

            val map = new HashMap<String, List<Object>>();
            map.put(ATTR_1, List.of(VALUE_1));
            map.put(ATTR_2, List.of(VALUE_2));
            map.put(ATTR_3, Arrays.asList("v3", "v4"));

            when(mockPrincipal.getAttributes()).thenReturn(map);
            when(mockPrincipal.getId()).thenReturn(PRINCIPAL_ID);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(mockPrincipal)
                .build();
            val attr = policy.getAttributes(context);
            assertEquals(2, attr.size());
            assertTrue(attr.containsKey(ATTR_1));
            assertTrue(attr.containsKey(ATTR_3));

            val data = SerializationUtils.serialize(policy);
            val p2 = SerializationUtils.deserializeAndCheckObject(data, ReturnAllowedAttributeReleasePolicy.class);
            assertNotNull(p2);
            assertEquals(p2.getAllowedAttributes(), policy.getAllowedAttributes());
        }

        @Test
        void verifyServiceAttributeDenyAllAttributes() throws Throwable {
            val policy = new DenyAllAttributeReleasePolicy();
            val p = mock(Principal.class);
            val map = new HashMap<String, List<Object>>();
            map.put("ATTR1", List.of(VALUE_1));
            map.put("ATTR2", List.of(VALUE_2));
            when(p.getAttributes()).thenReturn(map);
            when(p.getId()).thenReturn(PRINCIPAL_ID);
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(p)
                .build();
            val attr = policy.getAttributes(context);
            assertTrue(attr.isEmpty());
        }

        @Test
        void verifyServiceAttributeFilterAllAttributes() throws Throwable {
            val policy = new ReturnAllAttributeReleasePolicy();
            policy.setPrincipalIdAttribute("principalId");
            val mockPrincipal = mock(Principal.class);

            val map = new HashMap<String, List<Object>>();
            map.put(ATTR_1, List.of(VALUE_1));
            map.put(ATTR_2, List.of(VALUE_2));
            map.put(ATTR_3, Arrays.asList("v3", "v4"));

            when(mockPrincipal.getAttributes()).thenReturn(map);
            when(mockPrincipal.getId()).thenReturn(PRINCIPAL_ID);

            val registeredService = CoreAttributesTestUtils.getRegisteredService();
            when(registeredService.getUsernameAttributeProvider()).thenReturn(new RegisteredServiceUsernameAttributeProvider() {
                @Serial
                private static final long serialVersionUID = 771643288929352964L;

                @Override
                public String resolveUsername(final RegisteredServiceUsernameProviderContext context) {
                    return context.getPrincipal().getId();
                }
            });
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(mockPrincipal)
                .build();
            val attr = policy.getAttributes(context);
            assertEquals(attr.size(), map.size() + 1);

            val data = SerializationUtils.serialize(policy);
            val p2 = SerializationUtils.deserializeAndCheckObject(data, ReturnAllAttributeReleasePolicy.class);
            assertNotNull(p2);
        }

        @Test
        void checkServiceAttributeFilterAllAttributesWithCachingTurnedOn() throws Throwable {
            val policy = new ReturnAllAttributeReleasePolicy();

            val attributes = new HashMap<String, List<Object>>();
            attributes.put("values", Arrays.asList(new Object[]{"v1", "v2", "v3"}));
            attributes.put("cn", Arrays.asList(new Object[]{"commonName"}));
            attributes.put("username", Arrays.asList(new Object[]{"uid"}));

            val person = mock(PersonAttributes.class);
            when(person.getName()).thenReturn("uid");
            when(person.getAttributes()).thenReturn(attributes);

            val stub = new StubPersonAttributeDao(attributes);
            stub.setId("SampleStubRepository");

            val dao = new MergingPersonAttributeDaoImpl();
            dao.setPersonAttributeDaos(List.of(stub));

            ApplicationContextProvider.registerBeanIntoApplicationContext(this.applicationContext, dao, PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY);

            val repository = new CachingPrincipalAttributesRepository(TimeUnit.MILLISECONDS.name(), 100);
            repository.setAttributeRepositoryIds(Set.of(stub.getId()));
            val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("uid", Map.of("mail", List.of("final@example.com")));

            policy.setPrincipalAttributesRepository(repository);

            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(principal)
                .build();
            val attr = policy.getAttributes(context);
            assertEquals(attributes.size() + 1, attr.size());
        }

        @Test
        void verifyDefaults() throws Throwable {
            val policy = new RegisteredServiceAttributeReleasePolicy() {
                @Serial
                private static final long serialVersionUID = 6118477243447737445L;

                @Override
                public RegisteredServicePrincipalAttributesRepository getPrincipalAttributesRepository() {
                    return new DefaultPrincipalAttributesRepository();
                }

                @Override
                public Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
                    return context.getPrincipal().getAttributes();
                }
            };
            assertNull(policy.getConsentPolicy());
            assertNotNull(policy.getPrincipalAttributesRepository());
            assertTrue(policy.isAuthorizedToReleaseAuthenticationAttributes());
            assertFalse(policy.isAuthorizedToReleaseCredentialPassword());
            assertFalse(policy.isAuthorizedToReleaseProxyGrantingTicket());
            assertEquals(0, policy.getOrder());

            val principal = PrincipalFactoryUtils.newPrincipalFactory()
                .createPrincipal("uid", Map.of("mail", List.of("final@example.com")));
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(principal)
                .build();
            val attrs = policy.getConsentableAttributes(context);
            assertEquals(principal.getAttributes(), attrs);

            assertDoesNotThrow(() -> policy.setAttributeFilter(null));

        }
    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        CommonTestConfiguration.class,
        AttributeRepositoryTests.AttributeRepositoryTestConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class
    })
    class AttributeRepositoryTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void checkServiceAttributeFilterByAttributeRepositoryId() throws Throwable {
            val policy = new ReturnAllAttributeReleasePolicy();
            val repository = new CachingPrincipalAttributesRepository(TimeUnit.MILLISECONDS.name(), 0);
            val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("uid",
                Map.of("mail", List.of("final@example.com")));

            repository.setAttributeRepositoryIds(CollectionUtils.wrapSet("SampleStubRepository".toUpperCase(Locale.ENGLISH)));
            policy.setPrincipalAttributesRepository(repository);
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAttributesTestUtils.getRegisteredService())
                .service(CoreAttributesTestUtils.getService())
                .applicationContext(applicationContext)
                .principal(principal)
                .build();
            var attr = policy.getAttributes(context);
            assertEquals(4, attr.size());

            repository.setAttributeRepositoryIds(CollectionUtils.wrapSet("DoesNotExist"));
            policy.setPrincipalAttributesRepository(repository);
            attr = policy.getAttributes(context);
            assertEquals(1, attr.size());
        }

        @TestConfiguration(value = "AttributeRepositoryTestConfiguration", proxyBeanMethods = false)
        public static class AttributeRepositoryTestConfiguration {
            @Bean
            public PersonAttributeDao attributeRepository() {
                val attributes = new HashMap<String, List<Object>>();
                attributes.put("values", Arrays.asList(new Object[]{"v1", "v2", "v3"}));
                attributes.put("cn", Arrays.asList(new Object[]{"commonName"}));
                attributes.put("username", Arrays.asList(new Object[]{"uid"}));

                val person = mock(PersonAttributes.class);
                when(person.getName()).thenReturn("uid");
                when(person.getAttributes()).thenReturn(attributes);

                val stub = new StubPersonAttributeDao(attributes);
                stub.setId("SampleStubRepository");

                val dao = new MergingPersonAttributeDaoImpl();
                dao.setPersonAttributeDaos(List.of(stub));
                return dao;
            }
        }
    }
}
