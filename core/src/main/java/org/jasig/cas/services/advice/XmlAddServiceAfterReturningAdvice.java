/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

/**
 * Class to advise the ServiceRegistry.  In this implementation, any entry added to the
 * ServiceRegistry is automatically persisted in the XML file.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class XmlAddServiceAfterReturningAdvice implements AfterReturningAdvice {

    public void afterReturning(final Object returnValue, final Method method, final Object[] args,
        final Object target) throws Throwable {
        
        
        // TODO Auto-generated method stub

    }

}
