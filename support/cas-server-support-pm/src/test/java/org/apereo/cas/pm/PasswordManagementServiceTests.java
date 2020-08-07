package org.apereo.cas.pm;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("PasswordOps")
public class PasswordManagementServiceTests {
    @Test
    public void verifyOperation() {
        val service = new PasswordManagementService() {
        };
        assertFalse(service.change(RegisteredServiceTestUtils.getHttpBasedServiceCredentials(), new PasswordChangeRequest()));
        assertNull(service.findEmail("user@example.org"));
        assertNull(service.findPhone("user"));
        assertNull(service.findUsername("user@example.org"));
        assertNull(service.createToken("user"));
        assertNull(service.parseToken("user"));
        assertNotNull(service.getSecurityQuestions("casuser"));
    }

}
