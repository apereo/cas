package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
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
 * This is {@link SingleSignOnSessionCreatedActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowActions")
class SingleSignOnSessionCreatedActionTests {

    @Nested
    @TestPropertySource(properties = "cas.webflow.groovy.actions."
        + CasWebflowConstants.ACTION_ID_SINGLE_SIGNON_SESSION_CREATED
        + "=classpath:/SingleSignOnSessionCreated.groovy")
    class DefaultTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SINGLE_SIGNON_SESSION_CREATED)
        private Action action;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            assertEquals(1, context.getHttpServletResponse().getCookies().length);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.webflow.groovy.actions."
        + CasWebflowConstants.ACTION_ID_SINGLE_SIGNON_SESSION_CREATED
        + "=classpath:/Unknown12345.groovy")
    class UnknownScriptTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SINGLE_SIGNON_SESSION_CREATED)
        private Action action;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            assertNull(action.execute(context));
        }
    }
}
