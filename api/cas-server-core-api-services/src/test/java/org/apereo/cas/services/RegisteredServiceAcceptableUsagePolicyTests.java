package org.apereo.cas.services;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceAcceptableUsagePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class RegisteredServiceAcceptableUsagePolicyTests {

    @Test
    void verifyOperation() throws Throwable {
        val input = new RegisteredServiceAcceptableUsagePolicy() {
            @Serial
            private static final long serialVersionUID = -4878764188998002053L;
        };
        assertNull(input.getMessageCode());
        assertNull(input.getText());
        assertTrue(input.isEnabled());
    }

}
