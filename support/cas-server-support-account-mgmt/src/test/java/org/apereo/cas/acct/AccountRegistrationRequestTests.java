package org.apereo.cas.acct;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountRegistrationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
public class AccountRegistrationRequestTests {
    @Test
    public void verifyOperation() {
        val request = new AccountRegistrationRequest();
        request.putProperty("custom", "value");
        request.putProperty("complex", List.of(1, 2, 3));
        assertTrue(request.containsProperty("custom"));
        assertNull(request.getEmail());
        assertNull(request.getPhone());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNotNull(request.getProperty("complex", List.class));
    }
}
