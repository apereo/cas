package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractGraphicalAuthenticationTests;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptUserGraphicsForAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowAuthenticationActions")
class AcceptUserGraphicsForAuthenticationActionTests extends AbstractGraphicalAuthenticationTests {
    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("username", "casuser");

        val event = acceptUserGraphicsForAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue(WebUtils.containsGraphicalUserAuthenticationUsername(context));

        val credential = WebUtils.getCredential(context, UsernamePasswordCredential.class);
        assertNotNull(credential);
        assertEquals(0, credential.getPassword().length);
    }
}
