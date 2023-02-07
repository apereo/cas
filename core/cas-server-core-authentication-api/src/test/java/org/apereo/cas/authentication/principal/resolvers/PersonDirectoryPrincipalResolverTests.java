package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PersonDirectoryPrincipalResolverTests {

    private static final String ATTR_1 = "attr1";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @BeforeEach
    public void before() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    private PrincipalResolutionContext.PrincipalResolutionContextBuilder<?, ?> getPrincipalResolutionContextBuilder(final StubPersonAttributeDao attributeRepository) {
        return getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(), attributeRepository);
    }

    private PrincipalResolutionContext.PrincipalResolutionContextBuilder<?, ?> getPrincipalResolutionContextBuilder() {
        return getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            CoreAuthenticationTestUtils.getAttributeRepository());
    }

    private PrincipalResolutionContext.PrincipalResolutionContextBuilder<?, ?> getPrincipalResolutionContextBuilder(
        final PrincipalAttributesCoreProperties.MergingStrategyTypes casProperties,
        final StubPersonAttributeDao attributeRepository) {
        return PrincipalResolutionContext.builder()
            .attributeDefinitionStore(attributeDefinitionStore)
            .servicesManager(servicesManager)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties))
            .attributeRepository(attributeRepository)
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory());
    }

    @Test
    public void verifyOp() {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(true)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(true)
            .build();

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val credential = mock(Credential.class);
        val principal = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
    }

    @Test
    public void verifyOperation() {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(true)
            .resolveAttributes(false)
            .build();

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val credential = mock(Credential.class);
        val principal = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
    }

    @Test
    public void verifyNullPrincipal() {
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(new HashMap<>(0)))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(mock(Credential.class), Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNull(principal);
    }

    @Test
    public void verifyNoPrincipalAttrWithoutNull() {
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(new HashMap<>(0)))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .principalAttributeNames("cn")
            .build();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
    }

    @Test
    public void verifyUnknownPrincipalAttrWithNull() {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(true)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .principalAttributeNames("unknown")
            .build();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNull(principal);
    }

    @Test
    public void verifyNullAttributes() {
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(new HashMap<>(0)))
            .returnNullIfNoAttributes(true)
            .principalNameTransformer(String::trim)
            .principalAttributeNames(CoreAuthenticationTestUtils.CONST_USERNAME)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(c, Optional.empty());
        assertNull(principal);
    }

    @Test
    public void verifyNullAttributeValues() {
        val attributes = new ArrayList<>();
        attributes.add(null);
        val context = getPrincipalResolutionContextBuilder(new StubPersonAttributeDao(Map.of("a", attributes)))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val principal = resolver.resolve(new BasicIdentifiableCredential("a"));
        assertTrue(principal.getAttributes().containsKey("a"));
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .principalAttributeNames(CoreAuthenticationTestUtils.CONST_USERNAME)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(c, Optional.empty());
        assertNotNull(principal);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val principal = resolver.resolve(c, Optional.empty());
        assertNotNull(principal);
        assertNotEquals(CoreAuthenticationTestUtils.CONST_USERNAME, principal.getId());
        assertTrue(principal.getAttributes().containsKey("memberOf"));
    }

    @Test
    public void verifyChainingResolverOverwrite() {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);

        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("cn", List.of("originalCN"));
        attributes.put(ATTR_1, List.of("value1"));
        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attributes)),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertEquals(principal.getAttributes().size(),
            CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()).size() + 1);
        assertTrue(principal.getAttributes().containsKey(ATTR_1));
        assertTrue(principal.getAttributes().containsKey("cn"));
        assertNotEquals("originalCN", principal.getAttributes().get("cn"));
    }

    @Test
    public void verifyChainingResolver() {
        val context = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);

        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME,
                Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertEquals(principal.getAttributes().size(),
            CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()).size() + 1);
        assertTrue(principal.getAttributes().containsKey(ATTR_1));
    }

    @Test
    public void verifyChainingResolverOverwritePrincipal() {
        val context1 = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            new StubPersonAttributeDao(Collections.singletonMap("principal",
                CollectionUtils.wrap("changedPrincipal"))))
            .returnNullIfNoAttributes(false)
            .principalAttributeNames("principal")
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver2 = new PersonDirectoryPrincipalResolver(context2);

        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver, resolver2));

        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse",
                Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertEquals("changedPrincipal", principal.getId());
        assertEquals(7, principal.getAttributes().size());
        assertTrue(principal.getAttributes().containsKey(ATTR_1));
        assertTrue(principal.getAttributes().containsKey("principal"));
    }

    @Test
    public void verifyMultiplePrincipalAttributeNames() {
        val context1 = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .principalAttributeNames(" invalid, something")
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver2 = new PersonDirectoryPrincipalResolver(context2);
        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver, resolver2));

        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertEquals("principal-id", principal.getId());
    }

    @Test
    public void verifyMultiplePrincipalAttributeNamesNotFound() {
        val context1 = getPrincipalResolutionContextBuilder()
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = getPrincipalResolutionContextBuilder(casProperties.getAuthn().getAttributeRepository().getCore().getMerger(),
            new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))))
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .principalAttributeNames(" invalid, ")
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver2 = new PersonDirectoryPrincipalResolver(context2);
        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver, resolver2));

        val principal = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertNotNull(principal);
        assertEquals("test", principal.getId());
    }

    @Test
    public void verifyPrincipalIdViaCurrentPrincipal() {
        Stream.of(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE, PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED)
            .forEach(merger -> {
                val context = getPrincipalResolutionContextBuilder(merger, CoreAuthenticationTestUtils.getAttributeRepository())
                    .returnNullIfNoAttributes(true)
                    .principalAttributeNames("custom:attribute")
                    .useCurrentPrincipalId(true)
                    .resolveAttributes(true)
                    .build();

                val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PersonDirectoryPrincipalResolver.class, context);
                val credential = mock(Credential.class);
                val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("custom:attribute", List.of("customUserId")));
                val p = resolver.resolve(credential, Optional.of(principal),
                    Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
                    Optional.of(CoreAuthenticationTestUtils.getService()));
                assertNotNull(p);
                assertEquals("customUserId", p.getId());
            });

    }
}
