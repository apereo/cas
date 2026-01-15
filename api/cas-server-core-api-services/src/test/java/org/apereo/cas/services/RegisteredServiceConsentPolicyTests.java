package org.apereo.cas.services;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceConsentPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class RegisteredServiceConsentPolicyTests {

    @Test
    void verifyOperation() throws Throwable {
        val input = new RegisteredServiceConsentPolicy() {
            @Serial
            private static final long serialVersionUID = -4878764188998002053L;
        };
        assertEquals(0, input.size());
        assertEquals(0, input.getOrder());
        assertTrue(input.getStatus().isUndefined());
        assertTrue(input.getExcludedAttributes().isEmpty());
        assertTrue(input.getIncludeOnlyAttributes().isEmpty());
        assertNull(input.getExcludedServices());
    }

}
