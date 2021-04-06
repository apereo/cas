package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

    @Test
    public void verifyOp() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(true)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(true)
            .build();

        val resolver = new PersonDirectoryPrincipalResolver(context);
        val credential = mock(Credential.class);
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
    }

    @Test
    public void verifyOperation() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(true)
            .resolveAttributes(false)
            .build();

        val resolver = new PersonDirectoryPrincipalResolver(context);
        val credential = mock(Credential.class);
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
    }

    @Test
    public void verifyNullPrincipal() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(new StubPersonAttributeDao(new HashMap<>(0)))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context);
        val p = resolver.resolve(() -> null, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNull(p);
    }

    @Test
    public void verifyNoPrincipalAttrWithoutNull() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(new StubPersonAttributeDao(new HashMap<>(0)))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .principalAttributeNames("cn")
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context);
        val p = resolver.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
    }

    @Test
    public void verifyUnknownPrincipalAttrWithNull() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(true)
            .principalNameTransformer(String::trim)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .principalAttributeNames("unknown")
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context);
        val p = resolver.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNull(p);
    }

    @Test
    public void verifyNullAttributes() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(new StubPersonAttributeDao(new HashMap<>(0)))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(true)
            .principalNameTransformer(String::trim)
            .principalAttributeNames(CoreAuthenticationTestUtils.CONST_USERNAME)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = new PersonDirectoryPrincipalResolver(context);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(c, Optional.empty());
        assertNull(p);
    }

    @Test
    public void verifyNullAttributeValues() {
        val attributes = new ArrayList<>();
        attributes.add(null);
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(new StubPersonAttributeDao(Map.of("a", attributes)))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = new PersonDirectoryPrincipalResolver(context);
        val principal = resolver.resolve((Credential) () -> "a");
        assertTrue(principal.getAttributes().containsKey("a"));
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .principalAttributeNames(CoreAuthenticationTestUtils.CONST_USERNAME)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = new PersonDirectoryPrincipalResolver(context);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(c, Optional.empty());
        assertNotNull(p);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .principalAttributeNames("cn")
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = new PersonDirectoryPrincipalResolver(context);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(c, Optional.empty());
        assertNotNull(p);
        assertNotEquals(p.getId(), CoreAuthenticationTestUtils.CONST_USERNAME);
        assertTrue(p.getAttributes().containsKey("memberOf"));
    }

    @Test
    public void verifyChainingResolverOverwrite() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();

        val resolver = new PersonDirectoryPrincipalResolver(context);

        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("cn", List.of("originalCN"));
        attributes.put(ATTR_1, List.of("value1"));
        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attributes)),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals(p.getAttributes().size(),
            CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()).size() + 1);
        assertTrue(p.getAttributes().containsKey(ATTR_1));
        assertTrue(p.getAttributes().containsKey("cn"));
        assertNotEquals("originalCN", p.getAttributes().get("cn"));
    }

    @Test
    public void verifyChainingResolver() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context);

        val chain = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        chain.setChain(Arrays.asList(new EchoingPrincipalResolver(), resolver));
        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME,
                Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals(p.getAttributes().size(),
            CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose()).size() + 1);
        assertTrue(p.getAttributes().containsKey(ATTR_1));
    }

    @Test
    public void verifyChainingResolverOverwritePrincipal() {
        val context1 = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(new StubPersonAttributeDao(Collections.singletonMap("principal",
                CollectionUtils.wrap("changedPrincipal"))))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
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

        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse",
                Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("changedPrincipal", p.getId());
        assertEquals(7, p.getAttributes().size());
        assertTrue(p.getAttributes().containsKey(ATTR_1));
        assertTrue(p.getAttributes().containsKey("principal"));
    }

    @Test
    public void verifyMultiplePrincipalAttributeNames() {
        val context1 = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
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

        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("principal-id", p.getId());
    }

    @Test
    public void verifyMultiplePrincipalAttributeNamesNotFound() {
        val context1 = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context1);

        val context2 = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()))
            .attributeRepository(new StubPersonAttributeDao(Collections.singletonMap("something", CollectionUtils.wrap("principal-id"))))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
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

        val p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap(ATTR_1, List.of("value")))),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertEquals("test", p.getId());
    }

    @Test
    public void verifyPrincipalIdViaCurrentPrincipal() {
        Stream.of(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE, PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED)
            .forEach(merger -> {
                val context = PrincipalResolutionContext.builder()
                    .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(merger))
                    .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
                    .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
                    .returnNullIfNoAttributes(true)
                    .principalAttributeNames("custom:attribute")
                    .useCurrentPrincipalId(true)
                    .resolveAttributes(true)
                    .build();

                val resolver = new PersonDirectoryPrincipalResolver(context);
                val credential = mock(Credential.class);
                val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("custom:attribute", List.of("customUserId")));
                val p = resolver.resolve(credential, Optional.of(principal), Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
                assertNotNull(p);
                assertEquals("customUserId", p.getId());
            });

    }
}
