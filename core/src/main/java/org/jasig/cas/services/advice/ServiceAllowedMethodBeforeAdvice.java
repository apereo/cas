/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.jasig.cas.authentication.Service;
import org.jasig.cas.services.AuthenticatedService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ServiceAllowedMethodBeforeAdvice implements
    MethodBeforeAdvice {
    
    private ServiceRegistry serviceRegistry;

    /**
     * @see org.springframework.aop.MethodBeforeAdvice#before(java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
     */
    public final void before(Method method, Object[] args, Object target) throws Throwable {
        Service service = (Service) args[1];
        AuthenticatedService authenticatedService = this.serviceRegistry.getService(service.getId());
        
        if (authenticatedService == null) {
            throw new UnauthorizedServiceException();
        }
        
        beforeInternal(authenticatedService);
    }
    
    protected void beforeInternal(AuthenticatedService service) throws Exception {
        // this will be overwritten by extending classes
    }

}
