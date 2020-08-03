package org.apereo.cas.authentication.support.password;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordExpiringWarningMessageDescriptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class PasswordExpiringWarningMessageDescriptorTests {
    @Test
    public void verifyOperation() {
        val d = new PasswordExpiringWarningMessageDescriptor("DefaultMessage", 30);
        assertEquals(30, d.getDaysToExpiration());
        assertEquals("DefaultMessage", d.getDefaultMessage());
    }
}
