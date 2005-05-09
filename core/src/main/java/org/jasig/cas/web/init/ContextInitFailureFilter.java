/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.init;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * This Filter checks for a Throwable stored on initialization of {@link SafeContextLoaderListener}
 * or by {@link SafeDispatcherServlet}
 * and aborts the filter chain and forwards to an error JSP page in the case where
 * there were errors.
 * 
 * Using this filter only makes sense when you are also using the {@link SafeContextLoaderListener}
 * and the {@link SafeDispatcherServlet}.
 * 
 * By default this Filter forwards to /WEB-INF/view/jsp/brokenContext.jsp when it detects
 * that there was a failure at context initialization, but that path can be overridden by the
 * filter initialization parameter "errorTarget".
 * 
 * @version $Revision$ $Date$
 * @see SafeContextLoaderListener
 * @see SafeDispatcherServlet
 */
public final class ContextInitFailureFilter 
    implements Filter {

    /**
     * The name of the filter initialization parameter the value of which is the path
     * to which we should forward if SafeDelegatingContextListener caught exceptions
     * thrown by delegate ContextListeners.  If this filter initialization parameter is set and
     * we find a Throwable from context initizialization stored in the Context, we will forward to this
     * path.
     */
    public static final String ERROR_TARGET_PARAM = "errorTarget";
    
    /**
     * The default path to which we will forward when we detect errors from context initialization.  
     * If our filter initialization parameter is not set
     * and we find a Throwable from context initialization stored in the Context, we will forward to this path.
     * 
     * The value of this field is "/brokenContext.jsp".
     */
    public static final String DEFAULT_ERROR_TARGET = "/WEB-INF/view/jsp/brokenContext.jsp";
    
    /**
     * The name of the request attribute into which we will place the recorded
     * Throwable from the SafeContextLoaderListener when one was recorded, so that it is available to the error page.
     */
    public static final String THROWABLE_FROM_LISTENER_REQUEST_ATTRIBUTE = "throwable_from_listener";
    
    /**
     * The name of the request attribute into which we will place the recorded
     * Throwable from the SafeDispatcherServlet when one was recorded, so that it is available to the error page.
     */
    public static final String THROWABLE_FROM_SERVLET_REQUEST_ATTRIBUTE = "throwable_from_servlet";
    
    /**
     * The path to which we will forward if we discover that the SafeContextLoaderListener
     * recorded errors that otherwise would have aborted context initialization.
     */
    private String errorTarget = DEFAULT_ERROR_TARGET;
    
    private ServletContext servletContext;
    
    public void init(FilterConfig config) 
        throws ServletException {
        
        String targetParam = config.getInitParameter(ERROR_TARGET_PARAM);
        if (targetParam != null) {
            this.errorTarget = targetParam;
        }
        
        this.servletContext = config.getServletContext();
        

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) 
        throws IOException, ServletException {
        
        // we make these checks at runtime because the Throwable from the SafeDispatcherServlet
        // will not be set until initialization of SafeDispatcherServlet.
        
        // if there was a Throwable stored by SafeContextLoaderListener, we will capture it here.
        // if there was no Throwable, then we will be setting throwableFromContextInit to null.
        Throwable throwableFromContextInit = (Throwable) this.servletContext.getAttribute(SafeContextLoaderListener.CAUGHT_THROWABLE_KEY);
        
        Throwable throwableFromDispatcherServletInit = (Throwable) this.servletContext.getAttribute(SafeDispatcherServlet.CAUGHT_THROWABLE_KEY);
        
        if (throwableFromContextInit != null
                || throwableFromDispatcherServletInit != null) {
            // there was a throwable.
            // abort the filter processing chain and forward to our error page.

            request.setAttribute(THROWABLE_FROM_LISTENER_REQUEST_ATTRIBUTE , throwableFromContextInit);
            
            request.setAttribute(THROWABLE_FROM_SERVLET_REQUEST_ATTRIBUTE , throwableFromDispatcherServletInit);
            
            // this cast is safe because no one actually uses any other kind of ServletResponse.
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
            RequestDispatcher errorDispatcher = this.servletContext.getRequestDispatcher(this.errorTarget);
            errorDispatcher.forward(request, response);
            

        } else {
            
            // there was not a throwable.
            // proceed with the filter processing chain
            
            fc.doFilter(request, response);
            
        }
        
    }
    
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}
