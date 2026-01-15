package org.apereo.cas.pm.history;

import module java.base;
import org.apereo.cas.pm.impl.history.AmnesiacPasswordHistoryService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmnesiacPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("PasswordOps")
class AmnesiacPasswordHistoryServiceTests {

    @Test
    void verifyOperation() {
        val service = new AmnesiacPasswordHistoryService();
        assertTrue(service.fetchAll().isEmpty());
        assertTrue(service.fetch("casuser").isEmpty());

        assertDoesNotThrow(() -> {
            service.removeAll();
            service.remove("casuser");
        });
    }

}
