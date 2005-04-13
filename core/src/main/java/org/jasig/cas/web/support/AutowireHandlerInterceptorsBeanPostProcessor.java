/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class AutowireHandlerInterceptorsBeanPostProcessor implements
    BeanPostProcessor, ApplicationContextAware {

    /** Logger to log events and errors. */
    private final Log log = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    public Object postProcessBeforeInitialization(final Object bean, final String name)
        throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(final Object bean, final String name)
        throws BeansException {
        if (!(bean instanceof SimpleUrlHandlerMapping)) {
            return bean;
        }
        
        final SimpleUrlHandlerMapping mapping = (SimpleUrlHandlerMapping) bean;
        final Collection handlers = this.applicationContext.getBeansOfType(HandlerInterceptorAdapter.class).values();
        log.debug("Found " + handlers.size() + " HandlerInterceptors.  Attempting to register.");
        HandlerInterceptor[] handlerInterceptors = new HandlerInterceptor[handlers.size()];
        
        handlerInterceptors = (HandlerInterceptor[]) handlers.toArray(handlerInterceptors);
        
        mapping.setInterceptors(handlerInterceptors);
        log.debug("Successfully registered " + handlerInterceptors.length + " HandlerInterceptors.");

        return mapping;
    }

    
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
