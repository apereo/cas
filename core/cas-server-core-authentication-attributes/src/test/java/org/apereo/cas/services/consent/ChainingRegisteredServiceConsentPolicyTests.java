package org.apereo.cas.services.consent;

import org.apereo.cas.util.model.TriStateBoolean;

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
public class ChainingRegisteredServiceConsentPolicyTests {

    @Test
    public void verifyOperation() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(new DefaultRegisteredServiceConsentPolicy(Set.of("cn"), Set.of("givenName"))));
        chain.addPolicy(new DefaultRegisteredServiceConsentPolicy(Set.of("mail"), Set.of("address")));
        assertTrue(chain.getStatus().isUndefined());
        assertEquals(Set.of("cn", "mail"), chain.getExcludedAttributes());
        assertEquals(Set.of("givenName", "address"), chain.getIncludeOnlyAttributes());
    }

    @Test
    public void verifyStatusEnabled() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE),
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.TRUE)));
        assertTrue(chain.getStatus().isTrue());
    }

    @Test
    public void verifyStatusDisabled() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE),
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE)));
        assertTrue(chain.getStatus().isFalse());
    }

    @Test
    public void verifyStatusUndefined() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy(),
            new DefaultRegisteredServiceConsentPolicy()));
        assertTrue(chain.getStatus().isUndefined());
    }
}
