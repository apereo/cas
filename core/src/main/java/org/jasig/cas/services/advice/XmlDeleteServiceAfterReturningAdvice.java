/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

/**
 * Class to advice the service registry.  If a service was succesfully deleted, the backend
 * data store (an XML file) needs to be updated.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class XmlDeleteServiceAfterReturningAdvice implements
    AfterReturningAdvice {

    public void afterReturning(final Object returnValue, final Method method, final Object[] args,
        final Object target) throws Throwable {
        Boolean b = (Boolean) returnValue;
        
        
        if (b.booleanValue()) {
        // TODO Auto-generated method stub
        }

    }

}
