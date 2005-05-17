/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Resolver to resolve the credentials presented for an HTTP-based service to a
 * principal.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    /**
     * Method to return a simple Service Principal with the identifier set to be
     * the callback url.
     */
    public Principal resolvePrincipal(final Credentials credentials) {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials) credentials;

        return new SimpleService(serviceCredentials.getCallbackUrl()
            .toExternalForm());
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && HttpBasedServiceCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}
