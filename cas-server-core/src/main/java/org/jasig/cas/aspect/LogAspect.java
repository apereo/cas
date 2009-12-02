/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.aspect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 * 
 *
 * @version $Revision$ $Date$
 * @since 3.3.6
 */
@Aspect
public class LogAspect {

    @Around("(execution (public * org.jasig.cas..*.*(..))) && !(execution( * org.jasig.cas..*.set*(..)))")
    public Object traceMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object returnVal = null;
        final Log log = getLog(proceedingJoinPoint);
        final String methodName = proceedingJoinPoint.getSignature().getName();

        try {
            if (log != null && log.isTraceEnabled()) {
                final Object[] args = proceedingJoinPoint.getArgs();
                final String arguments;
                if (args == null || args.length == 0) {
                    arguments = "";
                } else {
                    final StringBuilder stringBuilder = new StringBuilder();
                    for (final Object o : args) {
                        stringBuilder.append(o != null ? o.toString() : null).append(", ");
                    }
                    arguments = stringBuilder.substring(0, stringBuilder.length()-3);
                }
                log.trace("Entering method [" + methodName + " with arguments [" + arguments + "]");
            }
            returnVal = proceedingJoinPoint.proceed();
            return returnVal;
        } finally {
            if (log != null && log.isTraceEnabled()) {
                log.trace("Leaving method [" + methodName + "] with return value [" + (returnVal != null ? returnVal.toString() : "null") + "].");
            }
        }
    }

    protected Log getLog(final JoinPoint joinPoint) {
        final Object target = joinPoint.getTarget();

        if (target != null) {
            return LogFactory.getLog(target.getClass());
        }

        return null;
    }

}
