package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogatePrincipalElectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class SurrogatePrincipalElectionStrategyTests {
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
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(new DefaultPrincipalFactory(), attributeRepository);
        val surrogatePrincipal = surrogatePrincipalBuilder.buildSurrogatePrincipal("cas-surrogate",
            primaryAuth.getPrincipal(), CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("cas-surrogate"));

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
