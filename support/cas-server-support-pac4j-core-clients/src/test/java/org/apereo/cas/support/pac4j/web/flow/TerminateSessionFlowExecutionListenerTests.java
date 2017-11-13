package org.apereo.cas.support.pac4j.web.flow;

import static org.junit.Assert.assertTrue;

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
 */
public class TerminateSessionFlowExecutionListenerTests {

    private TerminateSessionFlowExecutionListener listenerUnderTest;


    @Test
    public void httpSessionMustBeInvalidatedOnFlowSessionEnd() {
        // Prepare the input
        MockHttpServletRequest nativeRequest = new MockHttpServletRequest();
        MockHttpServletResponse nativeResponse = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        nativeRequest.setSession(session);
        MockServletContext servletContext = new MockServletContext();
        ServletExternalContext externalContext = new ServletExternalContext(servletContext, nativeRequest, nativeResponse);
        MockRequestContext rc = new MockRequestContext();
        rc.setExternalContext(externalContext);

        FlowSession flowSession = new MockFlowSession();
        String outcome = "EndStateId";
        AttributeMap<?> flowOutput = new LocalAttributeMap<>();

        // Run the tested listener
        listenerUnderTest.sessionEnded(rc, flowSession, outcome, flowOutput);

        // Check the session state
        assertTrue("The HTTP session should have been invalidated in the Terminate Session Listener.", session.isInvalid());

        // Check PAC4J state - PAC4J logout removes all user profiles form the request and the session.
        // The session cannot be checked here because it's already invalidated.
        Map<?,?> profilesFromRequest = (Map<?,?>) nativeRequest.getAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue("All PAC4J profiles on the HTTP request should have been cleared in the Terminate Session Listener.",
                profilesFromRequest.isEmpty());
    }


    @Before
    public void setUpTestedListener() {
        listenerUnderTest = new TerminateSessionFlowExecutionListener();
    }

}
