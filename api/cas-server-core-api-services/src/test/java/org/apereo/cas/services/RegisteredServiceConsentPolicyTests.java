package org.apereo.cas.services;

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
public class RegisteredServiceConsentPolicyTests {

    @Test
    public void verifyOperation() {
        val input = new RegisteredServiceConsentPolicy() {
            private static final long serialVersionUID = -4878764188998002053L;
        };
        assertEquals(0, input.size());
        assertEquals(0, input.getOrder());
        assertTrue(input.getStatus().isUndefined());
        assertTrue(input.getExcludedAttributes().isEmpty());
        assertTrue(input.getIncludeOnlyAttributes().isEmpty());
    }

}
