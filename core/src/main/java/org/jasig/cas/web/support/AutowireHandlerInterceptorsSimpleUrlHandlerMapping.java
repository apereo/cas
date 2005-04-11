/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.HandlerInterceptor;
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

    public void afterPropertiesSet() throws Exception {
        Collection handlers = getApplicationContext().getBeansOfType(HandlerInterceptor.class).values();
        HandlerInterceptor[] handlerInterceptors = new HandlerInterceptor[handlers.size()];
        
        handlerInterceptors = (HandlerInterceptor[]) handlers.toArray(handlerInterceptors);
        
        setInterceptors(handlerInterceptors);
    }
}
