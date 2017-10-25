package org.apereo.cas.support.pac4j.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

import com.google.common.base.Throwables;


/**
 * A Spring Flow execution listener that invalidates user session after the flow terminates.
 * 
 * Sometimes we need the session until the very last moment of the flow (e.g. during logout, we need the session to render the SAML SLO
 * request), that's why we invalidate it after the flow terminates.
 * 
 * @author jkacer
 */
public class TerminateSessionFlowExecutionListener extends FlowExecutionListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(TerminateSessionFlowExecutionListener.class);


    @Override
    public void sessionEnded(final RequestContext context, final FlowSession session, final String outcome, final AttributeMap<?> output) {
        super.sessionEnded(context, session, outcome, output);
        terminate(context);
    }


    /**
     * Destroys the session on flow termination.
     * 
     * @param context
     *            The flow request context.
     */
    protected void terminate(final RequestContext context) {
        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
            destroyApplicationSession(request, response);
            logger.debug("Terminated the application session successfully.");
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }


    /**
     * Destroys the session and performs PAC4J logout.
     * 
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     */
    protected void destroyApplicationSession(final HttpServletRequest request, final HttpServletResponse response) {
        logger.debug("Destroying application session");
        final ProfileManager<?> manager = WebUtils.getPac4jProfileManager(request, response);
        manager.logout();

        final HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
    }

}
