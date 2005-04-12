/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * Extension to regular SimpleUrlHandlerMapping that automagically
 * detects HandlerInteceptors from the ApplicationContext and registers
 * them with the HandlerMapping.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AutowireHandlerInterceptorsSimpleUrlHandlerMapping extends
    SimpleUrlHandlerMapping implements InitializingBean {

    /** Logger to log events and errors. */
    private final Log log = LogFactory.getLog(getClass());
    
    public void afterPropertiesSet() throws Exception {
        
        Collection handlers = getApplicationContext().getBeansOfType(HandlerInterceptorAdapter.class).values();
        log.debug("Found " + handlers.size() + " HandlerInterceptors.  Attempting to register.");
        HandlerInterceptor[] handlerInterceptors = new HandlerInterceptor[handlers.size()];
        
        handlerInterceptors = (HandlerInterceptor[]) handlers.toArray(handlerInterceptors);
        
        setInterceptors(handlerInterceptors);
        log.debug("Successfully registered " + handlerInterceptors.length + " HandlerInterceptors.");
    }
}
