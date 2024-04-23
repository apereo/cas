package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SendForgotUsernameInstructionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnabledIfListeningOnPort(port = 25000)
class SendForgotUsernameInstructionsActionTests {

    @Tag("Mail")
    @Nested
    class DefaultTests extends BasePasswordManagementActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        void verifyNoEmailOrUser() throws Throwable {
            val context = MockRequestContext.create();

            var result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

            context.setParameter("email", "123456");
            result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

            context.setParameter("email", "casuser@baddomain.org");
            result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
            assertFalse(context.getFlashScope().contains(Principal.class.getName()));
        }

        @Test
        void verifyOp() throws Throwable {
            val context = MockRequestContext.create();

            context.setParameter("email", "casuser@apereo.org");
            var result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertTrue(context.getFlashScope().contains(Principal.class.getName()));
        }
    }

    @Tag("Mail")
    @Nested
    @TestPropertySource(properties = "spring.boot.config.CasPersonDirectoryTestConfiguration.enabled=false")
    class NoPrincipalResolutionTests extends BasePasswordManagementActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        void verifyOpWithoutPrincipalResolution() throws Throwable {
            val context = MockRequestContext.create();

            context.setParameter("email", "casuser@apereo.org");
            var result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertFalse(context.getFlashScope().contains(Principal.class.getName()));
        }
    }
}
