package org.jasig.cas.web;

import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abtsract class to be extended by all controllers that may become a delegate.
 * All subclass must implement the canHandle method to say if they can handle a request or not.
 * @author Frederic Esnault
 * @version $Id$
 * @since 3.5
 */
public abstract class DelegateController extends AbstractController{
    /**
     * Determine if a DelegateController subclass can handle the current request.
     * @param request the current request
     * @param response the response
     * @return true if the controller can handler the request, false otherwise
     */
    public abstract boolean canHandle(HttpServletRequest request, HttpServletResponse response);


}
