/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.UnauthorizedServiceException;

/**
 * Method to check that if a service requires authentication,
 * Credentials are always passed in.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ServiceRequiresAuthenticationMethodBeforeAdvice extends
    ServiceAllowedMethodBeforeAdvice {

    /** The number of arguments we are expecting to process. */
    private static final int NUMBER_OF_ARGS = 3;

    protected void beforeInternal(final Method method, final Object[] args,
        final Object target, final RegisteredService service)
        throws UnauthorizedServiceException {
        if (args.length != NUMBER_OF_ARGS) {
            return;
        }

        final Credentials credentials = (Credentials) args[2];

        if (service.isForceAuthentication() && credentials == null) {
            throw new IllegalStateException(
                "Service always requires authentication.");
        }
    }
}
