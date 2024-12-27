package org.apereo.cas.services.consent;

import org.apereo.cas.configuration.support.TriStateBoolean;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceConsentPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class ChainingRegisteredServiceConsentPolicyTests {

    @Test
    void verifyOperation() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(new DefaultRegisteredServiceConsentPolicy(Set.of("cn"), Set.of("givenName"))));
        chain.addPolicy(new DefaultRegisteredServiceConsentPolicy(Set.of("mail"), Set.of("address")));
        assertTrue(chain.getStatus().isUndefined());
        assertEquals(Set.of("cn", "mail"), chain.getExcludedAttributes());
        assertEquals(Set.of("givenName", "address"), chain.getIncludeOnlyAttributes());
    }

    @Test
    void verifyStatusEnabled() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE),
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.TRUE)));
        assertTrue(chain.getStatus().isTrue());
    }

    @Test
    void verifyStatusDisabled() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE),
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE)));
        assertTrue(chain.getStatus().isFalse());
    }

    @Test
    void verifyStatusUndefined() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy(),
            new DefaultRegisteredServiceConsentPolicy()));
        assertTrue(chain.getStatus().isUndefined());
    }

    @Test
    void verifyExcludedServices() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.TRUE).setExcludedServices(Set.of("application1")),
            new DefaultRegisteredServiceConsentPolicy().setExcludedServices(Set.of("application2"))));
        assertTrue(chain.getStatus().isTrue());
        assertEquals(2, chain.getExcludedServices().size());
        assertTrue(chain.getExcludedServices().contains("application1"));
    }
}
