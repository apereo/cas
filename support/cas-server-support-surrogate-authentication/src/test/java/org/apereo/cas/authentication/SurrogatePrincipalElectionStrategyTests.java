package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
@Tag("Simple")
public class SurrogatePrincipalElectionStrategyTests {
    private static Principal buildSurrogatePrincipal(final String surrogateId,
                                                     final Authentication primaryAuth,
                                                     final IPersonAttributeDao attributeRepository) {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(PrincipalFactoryUtils.newPrincipalFactory(), attributeRepository,
            new SimpleSurrogateAuthenticationService(Map.of("test", List.of("surrogate")),
                mock(ServicesManager.class)));
        return surrogatePrincipalBuilder.buildSurrogatePrincipal(surrogateId,
            primaryAuth.getPrincipal(),
            RegisteredServiceTestUtils.getRegisteredService());
    }

    @Test
    public void verifyNominate() {
        val surrogate = buildSurrogatePrincipal("cas-surrogate",
            CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getAttributeRepository());

        val strategy = new SurrogatePrincipalElectionStrategy();
        val result = strategy.nominate(List.of(CoreAuthenticationTestUtils.getPrincipal("two"), surrogate),
            CoreAuthenticationTestUtils.getAttributes());
        assertEquals(result, surrogate);
    }

    @Test
    public void verifyOperation() {
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
        assertEquals(attributeRepository.getBackingMap().size(), principal.getAttributes().size());

        val result = attributeRepository.getBackingMap().keySet()
            .stream()
            .filter(key -> !principal.getAttributes().containsKey(key))
            .findAny();
        if (result.isPresent()) {
            fail();
        }
    }
}
