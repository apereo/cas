package org.jasig.cas.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class to be extended by all controllers that may become a delegate.
 * All subclass must implement the canHandle method to say if they can handle a request or not.
 * @author Frederic Esnault
 * @since 4.2.0
 */
public abstract class AbstractDelegateController {
    
    /** The logger. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Determine if a AbstractDelegateController subclass can handle the current request.
     * @param request the current request
     * @param response the response
     * @return true if the controller can handler the request, false otherwise
     */
    public abstract boolean canHandle(HttpServletRequest request, HttpServletResponse response);

    /**
     * Handle request internal.
     *
     * @param request the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception;

}
