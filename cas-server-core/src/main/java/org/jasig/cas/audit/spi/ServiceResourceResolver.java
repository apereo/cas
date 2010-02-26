/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;
import com.github.inspektr.audit.spi.AuditResourceResolver;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.util.AopUtils;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class ServiceResourceResolver implements AuditResourceResolver {

    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        final Service service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        return new String[] { retval.toString() + " for " + service.getId() };
    }

    public String[] resolveFrom(final JoinPoint joinPoint, final Exception ex) {
        final Service service = (Service) AopUtils.unWrapJoinPoint(joinPoint).getArgs()[1];
        return new String[] { service.getId() };
    }
}
