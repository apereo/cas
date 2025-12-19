package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SessionTerminationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Logout")
class SessionTerminationHandlerTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val processor = new SessionTerminationHandler() {
        };
        assertDoesNotThrow(() -> processor.beforeSessionTermination(context));
        assertDoesNotThrow(() -> processor.beforeSingleLogout(UUID.randomUUID().toString(), context));
        assertDoesNotThrow(() -> processor.afterSessionTermination(List.of(), context));
    }
}
