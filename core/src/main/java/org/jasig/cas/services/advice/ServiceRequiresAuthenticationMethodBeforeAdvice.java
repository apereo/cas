/* Copyright 2004 The JA-SIG Collaborative. All rights reserved. */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.services.AuthenticatedService;

/**
 * @author Scott Battaglia
 * @version $Id: ServiceRequiresAuthenticationMethodBeforeAdvice.java,v 1.1
 * 2005/03/08 20:39:23 sbattaglia Exp $
 */
public final class ServiceRequiresAuthenticationMethodBeforeAdvice extends
    ServiceAllowedMethodBeforeAdvice {

    protected void beforeInternal(Method method, Object[] args, Object target,
        AuthenticatedService service) throws Exception {
        if (args.length != 3)
            return;

        Credentials credentials = (Credentials)args[2];

        if (service.isForceAuthentication() && credentials == null) {
            throw new IllegalStateException(
                "Service always requires authentication.");
        }
    }

}
