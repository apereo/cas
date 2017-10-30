package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract logout action, which prevents caching on logout.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public abstract class AbstractLogoutAction extends AbstractAction {

    /**
     * The finish event in webflow.
     */
    public static final String FINISH_EVENT = "finish";

    /**
     * The front event in webflow.
     */
    public static final String FRONT_EVENT = "front";
    
    private static final String NO_CACHE = "no-cache";
    private static final String CACHE_CONTROL = "Cache-Control";

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        preventCaching(response);
        return doInternalExecute(request, response, context);
    }

    /**
     * Execute the logout action after invalidating the cache.
     *
     * @param request  the HTTP request.
     * @param response the HTTP response.
     * @param context  the webflow context.
     * @return the event triggered by this actions.
     */
    protected abstract Event doInternalExecute(HttpServletRequest request, HttpServletResponse response,
                                               RequestContext context);

    /**
     * Prevent caching by adding the appropriate headers.
     * Copied from the {@code preventCaching} method in the
     * {@link org.springframework.web.servlet.support.WebContentGenerator} class.
     *
     * @param response the HTTP response.
     */
    protected void preventCaching(final HttpServletResponse response) {
        response.setHeader("Pragma", NO_CACHE);
        response.setDateHeader("Expires", 1L);
        response.setHeader(CACHE_CONTROL, NO_CACHE);
        response.addHeader(CACHE_CONTROL, "no-store");
    }
}
