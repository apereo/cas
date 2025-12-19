package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.AbstractGraphicalAuthenticationTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrepareForGraphicalAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowAuthenticationActions")
class PrepareForGraphicalAuthenticationActionTests extends AbstractGraphicalAuthenticationTests {
    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val event = prepareLoginAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_GUA_GET_USERID, event.getId());
    }

    @Test
    void verifyMissingAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putGraphicalUserAuthenticationUsername(context, "casuser");
        val event = prepareLoginAction.execute(context);
        assertNull(event);
    }
}
