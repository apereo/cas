package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractGraphicalAuthenticationTests;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DisplayUserGraphicsBeforeAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowAuthenticationActions")
class DisplayUserGraphicsBeforeAuthenticationActionTests extends AbstractGraphicalAuthenticationTests {
    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("username", "casuser");
        val event = displayUserGraphicsBeforeAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue(WebUtils.containsGraphicalUserAuthenticationImage(context));
        assertTrue(WebUtils.containsGraphicalUserAuthenticationUsername(context));
    }

    @Test
    void verifyMissingUser() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertThrows(UnauthorizedServiceException.class, () -> displayUserGraphicsBeforeAuthenticationAction.execute(context));
    }

}
