/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

/**
 * HttpBasedServiceCredentialsToPrincipalResolver extracts the callbackUrl from
 * the HttpBasedServiceCredentials and constructs a SimpleService with the
 * callbackUrl as the unique Id.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.5 $ $Date: 2007/02/27 19:31:58 $
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
        return new SimpleWebApplicationServiceImpl(serviceCredentials.getCallbackUrl().toExternalForm());
    }

    /**
     * @return true if the credentials provided are not null and are assignable
     * from HttpBasedServiceCredentials, otherwise returns false.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null
            && HttpBasedServiceCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}
