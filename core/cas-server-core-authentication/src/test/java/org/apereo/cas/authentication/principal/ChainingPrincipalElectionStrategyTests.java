package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingPrincipalElectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class ChainingPrincipalElectionStrategyTests {
    @Test
    public void verifyOperationWithSingleAuthn() {
        val strategy = new ChainingPrincipalElectionStrategy(new DefaultPrincipalElectionStrategy());
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val principal = strategy.nominate(List.of(authentication), CoreAuthenticationTestUtils.getAttributes());
        assertNotNull(principal);
        assertEquals(principal, authentication.getPrincipal());
    }

    @Test
    public void verifyOperationWithMultipleAuthn() {
        val strategy1 = new DefaultPrincipalElectionStrategy();
        strategy1.setOrder(100);

        val strategy2 = new DefaultPrincipalElectionStrategy() {
            private static final long serialVersionUID = 332375002782221999L;

            @Override
            protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
                return authentications.stream().reduce((first, second) -> second).orElseThrow().getPrincipal();
            }
        };
        strategy2.setOrder(10);

        val strategy = new ChainingPrincipalElectionStrategy(strategy1, strategy2);
        
        val authentication1 = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser1"));
        val authentication2 = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser2"));
        val attributes = CoreAuthenticationTestUtils.getAttributes();
        val principal = strategy.nominate(List.of(authentication1, authentication2), attributes);
        assertNotNull(principal);
        assertEquals("casuser2", principal.getId());
    }

    @Test
    public void verifyOperationWithMultiplePrincipals() {
        val strategy1 = new DefaultPrincipalElectionStrategy();
        strategy1.setOrder(100);

        val strategy2 = new DefaultPrincipalElectionStrategy() {
            private static final long serialVersionUID = -6904928099265096984L;

            @Override
            public Principal nominate(final List<Principal> principals, final Map<String, List<Object>> attributes) {
                return principals.get(0);
            }
        };
        strategy2.setOrder(10);

        val strategy = new ChainingPrincipalElectionStrategy(strategy1, strategy2);

        val principal1 = CoreAuthenticationTestUtils.getPrincipal("casuser1");
        val principal2 = CoreAuthenticationTestUtils.getPrincipal("casuser2");
        val attributes = CoreAuthenticationTestUtils.getAttributes();
        
        val principal = strategy.nominate(List.of(principal1, principal2), attributes);
        assertNotNull(principal);
        assertEquals("casuser1", principal.getId());
    }
}
