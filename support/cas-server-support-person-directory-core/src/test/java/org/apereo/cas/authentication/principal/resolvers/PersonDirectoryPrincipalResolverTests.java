package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link PersonDirectoryPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Tag("Attributes")
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
class PersonDirectoryPrincipalResolverTests {

    private static final String ATTR_1 = "attr1";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    private ServicesManager servicesManager;

    @Mock
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @BeforeEach
    void before() {
        servicesManager = mock(ServicesManager.class);
    }

    private PrincipalResolutionContext.PrincipalResolutionContextBuilder<?, ?> getPrincipalResolutionContextBuilder(
        final StubPersonAttributeDao attributeRepository) {
        return getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(), attributeRepository);
    }

    private PrincipalResolutionContext.PrincipalResolutionContextBuilder<?, ?> getPrincipalResolutionContextBuilder() {
        return getPrincipalResolutionContextBuilder(
            casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            CoreAuthenticationTestUtils.getAttributeRepository());
    }

    private PrincipalResolutionContext.PrincipalResolutionContextBuilder<?, ?> getPrincipalResolutionContextBuilder(
        final PrincipalAttributesCoreProperties.MergingStrategyTypes casProperties,
        final StubPersonAttributeDao attributeRepository) {
        return PrincipalResolutionContext.builder()
            .attributeDefinitionStore(attributeDefinitionStore)
            .attributeRepositoryResolver(attributeRepositoryResolver)
            .servicesManager(servicesManager)
            .applicationContext(applicationContext)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties))
            .attributeRepository(attributeRepository)
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory());
    }

    @Test
    void verifyOp() throws Throwable {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(true)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(true)
            .build();

        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val credential = mock(Credential.class);
        val principal = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
    }

    @Test
    void verifyOperation() throws Throwable {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(true)
            .resolveAttributes(false)
            .build();

        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val credential = mock(Credential.class);
        val principal = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
    }

    @Test
    void verifyNullPrincipal() throws Throwable {
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(new HashMap<>()))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(mock(Credential.class), Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNull(principal);
    }

    @Test
    void verifyNoPrincipalAttrWithoutNull() throws Throwable {
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(new HashMap<>()))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .principalAttributeNames("cn")
            .build();
        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
    }

    @Test
    void verifyUnknownPrincipalAttrWithNull() throws Throwable {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(true)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .principalAttributeNames("unknown")
            .build();
        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNull(principal);
    }

    @Test
    void verifyNullAttributes() throws Throwable {
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(new HashMap<>()))
            .returnNullIfNoAttributes(true)
            .principalNameTransformer(String::trim)
            .principalAttributeNames(CoreAuthenticationTestUtils.CONST_USERNAME)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();

        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(credentials, Optional.empty());
        assertNull(principal);
    }

    @Test
    void verifyNullAttributeValues() throws Throwable {
        val attributes = new ArrayList<>();
        attributes.add(null);
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(Map.of("a", attributes)))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();

        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(new BasicIdentifiableCredential("a"));
        assertTrue(principal.getAttributes().containsKey("a"));
    }

    @Test
    void verifyNoAttributesWithPrincipal() throws Throwable {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .principalAttributeNames(CoreAuthenticationTestUtils.CONST_USERNAME)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();

        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(credentials, Optional.empty());
        assertNotNull(principal);
    }

    @Test
    void verifyAttributesWithPrincipal() throws Throwable {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();

        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(credentials, Optional.empty());
        assertNotNull(principal);
        assertNotEquals(CoreAuthenticationTestUtils.CONST_USERNAME, principal.getId());
        assertTrue(principal.getAttributes().containsKey("memberOf"));
    }

    @Test
    void verifyChainingResolverOverwrite() throws Throwable {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();

        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);

        val chain = buildResolver(List.of(new EchoingPrincipalResolver(), resolver));
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("cn", List.of("originalCN"));
        attributes.put(ATTR_1, List.of("value1"));
        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attributes)),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertEquals(7, principal.getAttributes().size());
        assertTrue(principal.getAttributes().containsKey(ATTR_1));
        assertTrue(principal.getAttributes().containsKey("cn"));
        assertNotEquals("originalCN", principal.getAttributes().get("cn"));
    }


    @Test
    void verifyChainingResolver() throws Throwable {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);

        val chain = buildResolver(List.of(new EchoingPrincipalResolver(), resolver));
        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME,
                Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertEquals(7, principal.getAttributes().size());
        assertTrue(principal.getAttributes().containsKey(ATTR_1));
    }

    @Test
    void verifyChainingResolverOverwritePrincipal() throws Throwable {
        val context1 = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            new StubPersonAttributeDao(Collections.singletonMap("principal", CollectionUtils.wrap("changedPrincipal"))))
            .returnNullIfNoAttributes(false)
            .principalAttributeNames("principal")
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver2 = new PersonDirectoryPrincipalResolver(context2);

        val chain = buildResolver(List.of(new EchoingPrincipalResolver(), resolver, resolver2));

        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse",
                Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertEquals("changedPrincipal", principal.getId());
        assertEquals(8, principal.getAttributes().size());
        assertTrue(principal.getAttributes().containsKey(ATTR_1));
        assertTrue(principal.getAttributes().containsKey("principal"));
    }

    @Test
    void verifyMultiplePrincipalAttributeNames() throws Throwable {
        val context1 = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .principalAttributeNames(" invalid, something")
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver2 = new PersonDirectoryPrincipalResolver(context2);
        val chain = buildResolver(List.of(new EchoingPrincipalResolver(), resolver, resolver2));

        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertEquals("principal-id", principal.getId());
    }

    @Test
    void verifyMultiplePrincipalAttributeNamesNotFound() throws Throwable {
        val context1 = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .principalAttributeNames(" invalid, ")
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver2 = new PersonDirectoryPrincipalResolver(context2);
        val chain = buildResolver(List.of(new EchoingPrincipalResolver(), resolver, resolver2));

        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertEquals("test", principal.getId());
    }

    @Test
    void verifyPrincipalIdViaCurrentPrincipal() {
        Stream.of(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE, PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED)
            .map(merger -> getPrincipalResolutionContextBuilder(merger, CoreAuthenticationTestUtils.getAttributeRepository())
                .returnNullIfNoAttributes(true)
                .principalAttributeNames("custom:attribute")
                .useCurrentPrincipalId(true)
                .resolveAttributes(true)
                .build())
            .map(context -> PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context))
            .forEach(Unchecked.consumer(resolver -> {
                val credential = mock(Credential.class);
                val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("custom:attribute", List.of("customUserId")));
                val resolved = resolver.resolve(credential, Optional.of(principal),
                    Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
                    Optional.of(CoreAuthenticationTestUtils.getService()));
                assertNotNull(resolved);
                assertEquals("customUserId", resolved.getId());
            }));
    }

    @Test
    void verifyAttributeRepositoryByService() {
        Stream.of(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED)
            .map(merger -> getPrincipalResolutionContextBuilder(merger, CoreAuthenticationTestUtils.getAttributeRepository())
                .returnNullIfNoAttributes(true)
                .principalAttributeNames("custom:attribute")
                .useCurrentPrincipalId(true)
                .resolveAttributes(true)
                .build())
            .map(context -> PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context))
            .forEach(Unchecked.consumer(resolver -> {
                val credential = mock(Credential.class);
                val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("custom:attribute", List.of("customUserId")));

                val attributePolicy = new RegisteredServiceAttributeReleasePolicy() {
                    @Serial
                    private static final long serialVersionUID = 6118477243447737445L;

                    @Override
                    public RegisteredServicePrincipalAttributesRepository getPrincipalAttributesRepository() {
                        val repo = mock(RegisteredServicePrincipalAttributesRepository.class);
                        when(repo.getAttributeRepositoryIds()).thenReturn(Set.of("StubPersonAttributeDao"));
                        return repo;
                    }

                    @Override
                    public Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
                        return context.getPrincipal().getAttributes();
                    }
                };

                val service = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
                val registeredService = CoreAuthenticationTestUtils.getRegisteredService(service.getId());
                lenient().when(registeredService.getAttributeReleasePolicy()).thenReturn(attributePolicy);
                lenient().when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

                val resolved = resolver.resolve(credential, Optional.of(principal),
                    Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
                    Optional.of(service));
                assertNotNull(resolved);
                assertEquals("customUserId", resolved.getId());
            }));
    }

    @Test
    void verifyPersonDirectoryOverrides() {
        val principal = new PersonDirectoryPrincipalResolverProperties();
        val personDirectory = new PersonDirectoryPrincipalResolverProperties();
        val principalResolutionContext = buildPrincipalResolutionContext(principal, personDirectory);
        assertFalse(principalResolutionContext.isUseCurrentPrincipalId());
        assertTrue(principalResolutionContext.isResolveAttributes());
        assertFalse(principalResolutionContext.isReturnNullIfNoAttributes());
        assertEquals(1, principalResolutionContext.getActiveAttributeRepositoryIdentifiers().size());
        assertTrue(principalResolutionContext.getActiveAttributeRepositoryIdentifiers().contains(PersonAttributeDao.WILDCARD));
        assertTrue(principalResolutionContext.getPrincipalAttributeNames().isEmpty());

        personDirectory.setUseExistingPrincipalId(TriStateBoolean.TRUE);
        personDirectory.setAttributeResolutionEnabled(TriStateBoolean.TRUE);
        personDirectory.setReturnNull(TriStateBoolean.TRUE);
        personDirectory.setAttributeResolutionEnabled(TriStateBoolean.FALSE);
        personDirectory.setActiveAttributeRepositoryIds("test1,test2");
        personDirectory.setPrincipalAttribute("principalAttribute");
        val principalResolutionContext2 = buildPrincipalResolutionContext(principal, personDirectory);
        assertTrue(principalResolutionContext2.isUseCurrentPrincipalId());
        assertFalse(principalResolutionContext2.isResolveAttributes());
        assertTrue(principalResolutionContext2.isReturnNullIfNoAttributes());
        assertEquals(3, principalResolutionContext2.getActiveAttributeRepositoryIdentifiers().size());
        assertEquals("principalAttribute", principalResolutionContext2.getPrincipalAttributeNames());

        principal.setUseExistingPrincipalId(TriStateBoolean.FALSE);
        principal.setAttributeResolutionEnabled(TriStateBoolean.FALSE);
        principal.setReturnNull(TriStateBoolean.FALSE);
        principal.setAttributeResolutionEnabled(TriStateBoolean.TRUE);
        principal.setActiveAttributeRepositoryIds("test1,test2,test3");
        principal.setPrincipalAttribute("principalAttribute2");
        val principalResolutionContext3 = buildPrincipalResolutionContext(principal, personDirectory);
        assertFalse(principalResolutionContext3.isUseCurrentPrincipalId());
        assertTrue(principalResolutionContext3.isResolveAttributes());
        assertFalse(principalResolutionContext3.isReturnNullIfNoAttributes());
        assertEquals(3, principalResolutionContext3.getActiveAttributeRepositoryIdentifiers().size());
        assertEquals("principalAttribute2", principalResolutionContext3.getPrincipalAttributeNames());

        val principalResolutionContext4 = buildPrincipalResolutionContext(personDirectory);
        assertTrue(principalResolutionContext4.isUseCurrentPrincipalId());
        assertFalse(principalResolutionContext4.isResolveAttributes());
        assertTrue(principalResolutionContext4.isReturnNullIfNoAttributes());
        assertEquals(2, principalResolutionContext4.getActiveAttributeRepositoryIdentifiers().size());
        assertEquals("principalAttribute", principalResolutionContext4.getPrincipalAttributeNames());
    }

    private PrincipalResolutionContext buildPrincipalResolutionContext(final PersonDirectoryPrincipalResolverProperties... properties) {
        return PersonDirectoryPrincipalResolver.buildPrincipalResolutionContext(
            applicationContext,
            PrincipalFactoryUtils.newPrincipalFactory(),
            new StubPersonAttributeDao(),
            CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD),
            servicesManager,
            attributeDefinitionStore,
            attributeRepositoryResolver,
            properties);
    }

    private PrincipalResolver buildResolver(final List<PrincipalResolver> resolvers) {
        return new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(),
            tenantExtractor, resolvers, casProperties);
    }

}
