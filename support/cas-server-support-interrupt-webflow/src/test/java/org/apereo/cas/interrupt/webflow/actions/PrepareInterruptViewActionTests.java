package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrepareInterruptViewActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowActions")
class PrepareInterruptViewActionTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val action = new PrepareInterruptViewAction();
        val event = action.execute(context);
        assertNull(event);
    }
}
