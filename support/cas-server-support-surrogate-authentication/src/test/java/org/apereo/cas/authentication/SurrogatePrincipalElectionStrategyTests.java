package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.ImpersonatedPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogatePrincipalElectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Impersonation")
class SurrogatePrincipalElectionStrategyTests {

    private Map<String, List<Object>> attributes = new HashMap<>(0);

    @BeforeEach
    public void initialize() {
        attributes = CollectionUtils.wrap(
                "formalName", CollectionUtils.wrapSet("cas"),
                "theName", CollectionUtils.wrapSet("user"),
                "sysuser", CollectionUtils.wrapSet("casuser"),
                "firstName", CollectionUtils.wrapSet("cas-first"),
                "lastName", CollectionUtils.wrapSet("cas-last"),
                "someOther", CollectionUtils.wrapSet("so that"),
                "count", CollectionUtils.wrapSet("is different from test utils"));
    }

    private static Principal buildSurrogatePrincipal(final String surrogateId,
                                                     final Authentication primaryAuth,
                                                     final IPersonAttributeDao attributeRepository) throws Throwable {
        val surrogatePrincipalBuilder = new DefaultSurrogateAuthenticationPrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(), attributeRepository,
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")),
                mock(ServicesManager.class)));
        return surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogateId,
            primaryAuth.getPrincipal(),
            RegisteredServiceTestUtils.getRegisteredService());
    }

    @Test
    void verifyNominate() throws Throwable {
        val surrogate = buildSurrogatePrincipal("cas-surrogate",
            CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getAttributeRepository());

        val strategy = new SurrogatePrincipalElectionStrategy();
        val result = strategy.nominate(CollectionUtils.wrapList(CoreAuthenticationTestUtils.getPrincipal("two"), surrogate),
            CoreAuthenticationTestUtils.getAttributes());
        assertEquals(result, surrogate);
    }

    @Test
    void verifyOperation() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();

        val authentications = new ArrayList<Authentication>();
        val primaryAuth = CoreAuthenticationTestUtils.getAuthentication("casuser");
        authentications.add(primaryAuth);

        val attributeRepository = CoreAuthenticationTestUtils.getAttributeRepository();
        val surrogatePrincipal = buildSurrogatePrincipal("cas-surrogate", primaryAuth, attributeRepository);

        authentications.add(CoreAuthenticationTestUtils.getAuthentication(surrogatePrincipal));
        val principal = strategy.nominate(authentications, (Map) attributes);
        assertNotNull(principal);
        assertEquals("cas-surrogate", principal.getId());
        assertEquals(attributeRepository.getBackingMap().size(), principal.getAttributes().size());

        val result = attributeRepository.getBackingMap().keySet()
            .stream()
            .filter(key -> !principal.getAttributes().containsKey(key))
            .findAny();
        if (result.isPresent()) {
            fail();
        }
    }

    @Test
    void verifyAttributes() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser1");
        val attributesCalculated = strategy
                .getPrincipalAttributesForPrincipal(principal, attributes);
        assertNotNull(attributesCalculated);
        assertEquals(attributes.size(), attributesCalculated.size());

        val surrogate = new ImpersonatedPrincipal(CoreAuthenticationTestUtils.getPrincipal("casuser2"));
        val attributesFromSurrogate = strategy
                .getPrincipalAttributesForPrincipal(surrogate, attributes);
        assertNotNull(attributesFromSurrogate);
        assertEquals(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap().size(), attributesFromSurrogate.size());
    }

    @Test
    void verifyOperationMultipleAuthnNoSurrogate() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();

        val authentication1 = CoreAuthenticationTestUtils.
                getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser1"));
        val authentication2 = CoreAuthenticationTestUtils.
                getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser2"));
        val principal = strategy.nominate(List.of(authentication1, authentication2), attributes);
        assertNotNull(principal);
        assertEquals("casuser2", principal.getId());
        assertEquals(attributes.size(), principal.getAttributes().size());
    }

    @Test
    void verifyOperationAuthnSurrogate() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();

        val attributeRepository = CoreAuthenticationTestUtils.getAttributeRepository();
        val primaryAuth = CoreAuthenticationTestUtils.
                getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser1"));
        val surrogatePrincipal = buildSurrogatePrincipal("cas-surrogate", primaryAuth, attributeRepository);

        val authentications = new ArrayList<Authentication>();
        authentications.add(CoreAuthenticationTestUtils.getAuthentication(surrogatePrincipal));
        val principal = strategy.nominate(authentications, attributes);
        assertNotNull(principal);
        assertEquals("cas-surrogate", principal.getId());
        assertEquals(attributeRepository.getBackingMap().size(), principal.getAttributes().size());
    }

    @Test
    void verifyOperationPrincipalsNoSurrogate() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();

        val attributeRepositoryType0 = CoreAuthenticationTestUtils.getAttributeRepository();
        attributeRepositoryType0.getBackingMap().put("some", CollectionUtils.wrapList("authentication zero"));
        val attributeRepositoryType1 = CoreAuthenticationTestUtils.getAttributeRepository();
        attributeRepositoryType1.getBackingMap().put("another", CollectionUtils.wrapList("authentication one"));

        val authentications = new ArrayList<Authentication>();
        val authnFromType0 = CoreAuthenticationTestUtils.getAuthentication("casuser", attributeRepositoryType0.getBackingMap());
        val authnFromType1 = CoreAuthenticationTestUtils.getAuthentication("casuser", attributeRepositoryType1.getBackingMap());
        authentications.add(authnFromType0);
        authentications.add(authnFromType1);

        val principal = strategy.nominate(authentications, (Map) attributes);
        assertNotNull(principal);
        assertEquals("casuser", principal.getId());
        assertEquals(attributes.size(), principal.getAttributes().size());

        val result = attributes.keySet()
                .stream()
                .filter(key -> !principal.getAttributes().containsKey(key))
                .findAny();
        if (result.isPresent()) {
            fail();
        }
    }

    @Test
    void verifyOperationPrincipalsSurrogate() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();

        val attributeRepository = CoreAuthenticationTestUtils.getAttributeRepository();

        val attributeRepositoryType0 = CoreAuthenticationTestUtils.getAttributeRepository();
        attributeRepositoryType0.getBackingMap().put("some", CollectionUtils.wrapList("authentication zero"));
        val attributeRepositoryType1 = CoreAuthenticationTestUtils.getAttributeRepository();
        attributeRepositoryType1.getBackingMap().put("another", CollectionUtils.wrapList("authentication one"));

        val principals = new ArrayList<Principal>();

        val primaryAuth = CoreAuthenticationTestUtils.getAuthentication("casuser", attributeRepository.getBackingMap());
        principals.add(primaryAuth.getPrincipal());

        val surrogatePrincipal = buildSurrogatePrincipal("cas-surrogate", primaryAuth, attributeRepository);
        principals.add(surrogatePrincipal);
        assertEquals(2, principals.size());

        val principal1 = strategy.nominate(principals, (Map) attributes);
        assertEquals(1, principals.size());
        assertNotNull(principal1);
        assertEquals("cas-surrogate", principal1.getId());
        assertEquals(attributeRepository.getBackingMap().size(), principal1.getAttributes().size());

        val result1 = attributeRepository.getBackingMap().keySet()
                .stream()
                .filter(key -> !principal1.getAttributes().containsKey(key))
                .findAny();
        if (result1.isPresent()) {
            fail();
        }
    }

    @Test
    void verifyMultiPrincipalsWithNoAttributes() throws Throwable {
        val strategy = new SurrogatePrincipalElectionStrategy();
        val attributes = CollectionUtils.<String, List<Object>>wrap(
            "primaryName1", CollectionUtils.wrapList("cas"),
            "primaryName2", CollectionUtils.wrapList("user"));

        val principals = new ArrayList<Principal>();
        val primaryPrincipal1 = CoreAuthenticationTestUtils.getPrincipal("primary", new HashMap<>());
        principals.add(primaryPrincipal1);

        val attributeRepository = CoreAuthenticationTestUtils.getAttributeRepository();
        val surrogatePrincipal = buildSurrogatePrincipal("cas-surrogate",
            CoreAuthenticationTestUtils.getAuthentication(primaryPrincipal1), attributeRepository);
        principals.add(surrogatePrincipal);

        val primaryPrincipal2 = CoreAuthenticationTestUtils.getPrincipal("primary", attributes);
        principals.add(primaryPrincipal2);

        val principal = (SurrogatePrincipal) strategy.nominate(principals, Map.of());
        assertNotNull(principal);
        assertEquals("cas-surrogate", principal.getId());
        assertEquals(attributeRepository.getBackingMap().size(), principal.getAttributes().size());
        assertEquals("primary", principal.getPrimary().getId());
        assertEquals(attributes, principal.getPrimary().getAttributes());
    }
}
