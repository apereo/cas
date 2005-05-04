/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.util.UniqueTokenIdGenerator;
import org.jasig.cas.web.support.ViewNames;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * ExceptionResolver to map TicketExceptions to a view with the proper error
 * model.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class TicketExceptionHandlerExceptionResolver implements
    HandlerExceptionResolver {

    /** Logger instance. */
    private final Log log = LogFactory
        .getLog(TicketExceptionHandlerExceptionResolver.class);
    
    private LoginController loginController;

    public TicketExceptionHandlerExceptionResolver() {
        super();
    }

    public ModelAndView resolveException(final HttpServletRequest request,
        final HttpServletResponse response, final Object handler,
        final Exception exception) {

        if (!(exception instanceof TicketException)) {
            log.debug("Exception detected was: "
                + exception.getClass().getName());
            return null;
        }

        log.debug("Detected TicketException. Showing error page.");
        final TicketException t = (TicketException) exception;
        BindException errors = new BindException(
            new UsernamePasswordCredentials(), "credentials");
        errors.reject(t.getCode());

        final Map model = new HashMap();
        model.putAll(errors.getModel());
        
        try {
            model.putAll(this.loginController.referenceData(request));
        } catch (Exception e) {
            // nothing to do
        }

        return new ModelAndView(ViewNames.CONST_LOGON, model);
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }
    

}
