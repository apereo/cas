/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import org.jasig.cas.services.AuthenticatedService;
import org.jasig.cas.services.UnauthorizedServiceException;

/**
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ServiceAllowedForceAuthenticationMethodBeforeAdvice extends
    ServiceAllowedMethodBeforeAdvice {

    protected void beforeInternal(AuthenticatedService service) throws Exception {
        if (service.isForceAuthentication()) {
            throw new UnauthorizedServiceException();
        }
    }
}
