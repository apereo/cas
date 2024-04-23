package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
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
    private static Principal buildSurrogatePrincipal(final String surrogateId,
                                                     final Authentication primaryAuth,
                                                     final PersonAttributeDao attributeRepository) throws Throwable {
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
        val attributes = CollectionUtils.wrap(
            "formalName", CollectionUtils.wrapSet("cas"),
            "theName", CollectionUtils.wrapSet("user"),
            "sysuser", CollectionUtils.wrapSet("casuser"),
            "firstName", CollectionUtils.wrapSet("cas-first"),
            "lastName", CollectionUtils.wrapSet("cas-last"));

        val authentications = new ArrayList<Authentication>();
        val primaryAuth = CoreAuthenticationTestUtils.getAuthentication("casuser");
        authentications.add(primaryAuth);

        val attributeRepository = CoreAuthenticationTestUtils.getAttributeRepository();
        val surrogatePrincipal = buildSurrogatePrincipal("cas-surrogate", primaryAuth, attributeRepository);

        authentications.add(CoreAuthenticationTestUtils.getAuthentication(surrogatePrincipal));
        val principal = strategy.nominate(authentications, (Map) attributes);
        assertNotNull(principal);
        assertEquals("cas-surrogate", principal.getId());
        assertEquals(6, principal.getAttributes().size());

        val result = attributeRepository.getBackingMap().keySet()
            .stream()
            .filter(key -> !principal.getAttributes().containsKey(key))
            .findAny();
        if (result.isPresent()) {
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
        assertEquals(6, principal.getAttributes().size());
        assertEquals("primary", principal.getPrimary().getId());
        assertEquals(attributes, principal.getPrimary().getAttributes());
    }
}
