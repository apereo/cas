package org.apereo.cas.support.pac4j.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.google.common.base.Throwables;


/**
 * A Spring Flow action that invalidates user session.
 * 
 * Intended to be used at the end of the flow, in the "on-end" section.
 * 
 * Sometimes we need the session until the very last moment of the flow (e.g. during logout, we need the session to render the SAML SLO
 * request), that's why we invalidate it after the flow terminates.
 * 
 * It contains a subset of code from the original {@code TerminateSessionAction} that is not executed on deferred session termination.
 * So these two actions are complementary:
 * <ul>
 * <li>Either only {@code TerminateSessionAction} is used,</li>
 * <li>or both are used, {@code TerminateSessionAction} does everything except actual HTTP session termination and PAC4J logout, which is
 * done in {@code LimitedTerminateSessionAction}.</li>
 * </ul>
 * 
 * @author jkacer
 * 
 * @since 5.2.0
 */
public class LimitedTerminateSessionAction extends AbstractAction {

    private final Logger logger2 = LoggerFactory.getLogger(LimitedTerminateSessionAction.class);


    @Override
    protected Event doExecute(final RequestContext context) {
        terminate(context);
        return null;
    }


    /**
     * Destroys the session on flow termination.
     * 
     * @param context
     *            The flow request context.
     */
    protected void terminate(final RequestContext context) {
        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            destroyApplicationSession(request, response);
            logger2.debug("Terminated the application session successfully.");
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
        logger2.debug("Destroying application session");
        final ProfileManager<?> manager = Pac4jUtils.getPac4jProfileManager(request, response);
        manager.logout();

        final HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
    }


}
