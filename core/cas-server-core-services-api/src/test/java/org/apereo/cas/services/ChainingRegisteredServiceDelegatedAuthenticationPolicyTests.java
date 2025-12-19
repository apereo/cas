package org.apereo.cas.services;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceDelegatedAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
class ChainingRegisteredServiceDelegatedAuthenticationPolicyTests {
    @Test
    void verifySelectionStrategy() {
        val chain = new ChainingRegisteredServiceDelegatedAuthenticationPolicy();
        val p1 = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        val p2 = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        p2.setSelectionStrategy("groovy { providers.first()}");
        chain.addStrategy(p1);
        chain.addStrategy(p2);
        assertTrue(chain.getSelectionStrategy().startsWith("groovy {"));
    }
}
