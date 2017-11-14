package org.apereo.cas.support.pac4j.web.flow;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.Pac4jConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test of {@link TerminateSessionFlowExecutionListener}.
 * 
 * @author jkacer
 * 
 * @since 5.2.0
 */
public class TerminateSessionFlowExecutionListenerTests {

    private TerminateSessionFlowExecutionListener listenerUnderTest;


    @Test
    public void httpSessionMustBeInvalidatedOnFlowSessionEnd() {
        // Prepare the input
        final MockHttpServletRequest nativeRequest = new MockHttpServletRequest();
        final MockHttpServletResponse nativeResponse = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        nativeRequest.setSession(session);
        final MockServletContext servletContext = new MockServletContext();
        final ServletExternalContext externalContext = new ServletExternalContext(servletContext, nativeRequest, nativeResponse);
        final MockRequestContext rc = new MockRequestContext();
        rc.setExternalContext(externalContext);

        final FlowSession flowSession = new MockFlowSession();
        final String outcome = "EndStateId";
        final AttributeMap<?> flowOutput = new LocalAttributeMap<>();

        // Run the tested listener
        listenerUnderTest.sessionEnded(rc, flowSession, outcome, flowOutput);

        // Check the session state
        assertTrue("The HTTP session should have been invalidated in the Terminate Session Listener.", session.isInvalid());

        // Check PAC4J state - PAC4J logout removes all user profiles form the request and the session.
        // The session cannot be checked here because it's already invalidated.
        final Map<?, ?> profilesFromRequest = (Map<?, ?>) nativeRequest.getAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue("All PAC4J profiles on the HTTP request should have been cleared in the Terminate Session Listener.",
                profilesFromRequest.isEmpty());
    }


    @Before
    public void setUpTestedListener() {
        listenerUnderTest = new TerminateSessionFlowExecutionListener();
    }

}
