package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

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
                                     + CasWebflowConstants.ACTION_ID_SINGLE_SIGON_SESSION_CREATED
                                     + "=classpath:/SingleSignOnSessionCreated.groovy")
    @SuppressWarnings("ClassCanBeStatic")
    class DefaultTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SINGLE_SIGON_SESSION_CREATED)
        private Action action;

        @Test
        void verifyOperation() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            assertEquals(1, response.getCookies().length);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.webflow.groovy.actions."
                                     + CasWebflowConstants.ACTION_ID_SINGLE_SIGON_SESSION_CREATED
                                     + "=classpath:/Unknown12345.groovy")
    @SuppressWarnings("ClassCanBeStatic")
    class UnknownScriptTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SINGLE_SIGON_SESSION_CREATED)
        private Action action;

        @Test
        void verifyOperation() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            assertNull(action.execute(context));
        }
    }
}
