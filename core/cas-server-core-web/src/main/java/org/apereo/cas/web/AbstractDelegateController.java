package org.apereo.cas.web;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class to be extended by all controllers that may become a delegate.
 * All subclass must implement the canHandle method to say if they can handle a request or not.
 * @author Frederic Esnault
 * @since 4.2.0
 */
@Controller
public abstract class AbstractDelegateController implements ApplicationContextAware {

    /** Application context. */
    protected ApplicationContext applicationContext;

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

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
