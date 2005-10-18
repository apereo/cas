/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.lang.reflect.Method;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.services.RegisteredService;

/**
 * Advice to confirm that if a service forces the concept of a renew = true,
 * (for example, opting out of Single-sign on) then they need to have passed in
 * a set of Credentials which means they called the grantServiceTicket with the
 * third parameter set to a form of Credentials.
 * <p>
 * The behavior for this advice is only defined for the grantedServiceTicket
 * methods.
 * </p>
 * <p>
 * Please check the super class for any required properties.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ServiceRequiresAuthenticationMethodBeforeAdvice extends
    ServiceAllowedMethodBeforeAdvice {

    /** The number of arguments we are expecting to process. */
    private static final int NUMBER_OF_ARGS = 3;

    /**
     * @throws IllegalStateException if the service requires authentication and
     * Credentials are not provided.
     */
    protected void beforeInternal(final Method method, final Object[] args,
        final Object target, final RegisteredService service) {
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
