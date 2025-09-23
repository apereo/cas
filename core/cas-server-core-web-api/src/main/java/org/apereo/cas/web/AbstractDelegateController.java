package org.apereo.cas.web;

import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Abstract class to be extended by all controllers that may become a delegate.
 * All subclass must implement the canHandle method to say if they can handle a request or not.
 *
 * @author Frederic Esnault
 * @since 4.2.0
 */
@Setter
public abstract class AbstractDelegateController extends AbstractController implements ApplicationContextAware {

    /**
     * Application context.
     */
    protected ApplicationContext applicationContext;

    /**
     * Determine if a subclass can handle the current request.
     *
     * @param request  the current request
     * @param response the response
     * @return true if the controller can handler the request, false otherwise
     */
    public abstract boolean canHandle(HttpServletRequest request, HttpServletResponse response);

    /**
     * Handle request internal.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Throwable the throwable
     */
    protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Throwable;
}
