package org.apereo.cas.pm.history;

import org.apereo.cas.pm.impl.history.AmnesiacPasswordHistoryService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmnesiacPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("PasswordOps")
public class AmnesiacPasswordHistoryServiceTests {

    @Test
    public void verifyOperation() {
        val service = new AmnesiacPasswordHistoryService();
        assertTrue(service.fetchAll().isEmpty());
        assertTrue(service.fetch("casuser").isEmpty());

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                service.removeAll();
                service.remove("casuser");
            }
        });
    }

}
