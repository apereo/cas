/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;
import org.inspektr.audit.spi.AuditableResourceResolver;
import org.jasig.cas.authentication.principal.Service;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class ServiceResourceResolver implements AuditableResourceResolver {

    public String resolveFrom(final JoinPoint joinPoint, final Object retval) {
        final Service service = (Service) joinPoint.getArgs()[1];
        return retval.toString() + " for " + service.getId();
    }

    public String resolveFrom(final JoinPoint joinPoint, final Exception ex) {
        final Service service = (Service) joinPoint.getArgs()[1];
        return service.getId();
    }
}
