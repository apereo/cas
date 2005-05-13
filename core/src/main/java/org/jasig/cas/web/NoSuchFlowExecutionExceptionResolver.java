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

import org.jasig.cas.web.support.WebConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class NoSuchFlowExecutionExceptionResolver implements
    HandlerExceptionResolver, InitializingBean {
    
    private static final String DEFAULT_LOGIN_URL = "login";
    
    private String loginUrl;

    public ModelAndView resolveException(final HttpServletRequest request,
        final HttpServletResponse response, final Object handler,
        final Exception exception) {
        
        if (!exception.getClass().equals(NoSuchFlowExecutionException.class)) {
            return null;
        }
        
        Map model = new HashMap();
        model.put(WebConstants.SERVICE, request.getParameter(WebConstants.SERVICE));
        model.put(WebConstants.GATEWAY, request.getParameter(WebConstants.GATEWAY));
        model.put(WebConstants.RENEW, request.getParameter(WebConstants.RENEW));

        return new ModelAndView(new RedirectView(this.loginUrl), model);
    }

    public void afterPropertiesSet() throws Exception {
        if (this.loginUrl == null) {
            this.loginUrl = DEFAULT_LOGIN_URL;
        }
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

}
