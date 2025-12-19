package org.apereo.cas.pm.jdbc;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("JDBC")
@TestPropertySource(properties = "cas.authn.pm.history.core.enabled=true")
class JdbcPasswordHistoryServiceTests extends BaseJdbcPasswordManagementServiceTests {
    @Test
    void verifyOperation() throws Throwable {
        val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
        assertFalse(passwordHistoryService.exists(request));
        assertTrue(passwordHistoryService.store(request));
        assertTrue(passwordHistoryService.exists(request));
        assertFalse(passwordHistoryService.fetchAll().isEmpty());
        assertFalse(passwordHistoryService.fetch("casuser").isEmpty());
        
        passwordHistoryService.remove("casuser");
        assertFalse(passwordHistoryService.exists(request));
    }
}
