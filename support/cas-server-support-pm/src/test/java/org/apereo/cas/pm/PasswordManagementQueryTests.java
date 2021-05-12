package org.apereo.cas.pm;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementQueryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("PasswordOps")
public class PasswordManagementQueryTests {

    @Test
    public void verifyOperation() {
        val uid = UUID.randomUUID().toString();
        val query = PasswordManagementQuery.builder().username(uid).build();
        query.attribute("address1", "Some Address");
        query.securityQuestion("Question1", "Answer1");
        assertNotNull(query.toString());
        assertNotNull(query.find("address1", String.class));
        assertEquals(query, PasswordManagementQuery.builder().username(uid).build());
    }
}
