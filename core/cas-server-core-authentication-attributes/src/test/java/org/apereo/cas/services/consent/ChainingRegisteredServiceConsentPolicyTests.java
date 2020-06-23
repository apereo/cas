package org.apereo.cas.services.consent;

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
        assertTrue(chain.isEnabled());
        assertEquals(Set.of("cn", "mail"), chain.getExcludedAttributes());
        assertEquals(Set.of("givenName", "address"), chain.getIncludeOnlyAttributes());
    }
}
